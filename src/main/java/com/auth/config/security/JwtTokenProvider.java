package com.auth.config.security;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.auth.http.exception.UnauthorizedException;
import com.auth.i18n.MessageByLocaleService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider extends AbstractUserDetailsAuthenticationProvider {

	private static final String UNAUTHORIZED_MSG = "unauthorized.msg";

	private static final String INVALID_SESSION = "invalid.session";

	private Logger logger = LogManager.getLogger(this.getClass());

	@Value("${jwt.secret}")
	private String jwtSecret;
	@Value("${jwt.expirationInMs}")
	private int jwtExpirationInMs;

	@Autowired
	private CustomUserDetailService userDetailService;
	@Autowired
	private MessageByLocaleService messageService;

	public String generateToken(Authentication authentication) {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

		// Set claims with the administrator id in the future
		return Jwts.builder().setSubject(Long.toString(userPrincipal.getId())).setIssuedAt(new Date())
				.setExpiration(expiryDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact();

	}

	public Long getUserIdFromJWT(String token) {
		Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

		return Long.parseLong(claims.getSubject());
	}

	public String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	public boolean validateToken(String authToken) throws UnauthorizedException {
		boolean isValid = false;
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			isValid = true;
		} catch (ExpiredJwtException ex) {
			logger.error("Expired JWT token");
			throw new UnauthorizedException(messageService.getMessage(INVALID_SESSION));
		} catch (Exception ex) {
			logger.error("Error: " + ex.getMessage() + " Class: " + ex.getClass());
			throw new UnauthorizedException(messageService.getMessage(UNAUTHORIZED_MSG));
		}
		return isValid;
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		// TODO Auto-generated method stub

	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		return userDetailService.loadUserByUsername(username);
	}

}