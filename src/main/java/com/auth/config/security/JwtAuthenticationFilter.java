package com.auth.config.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth.http.exception.UnauthorizedException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private Logger logger = LogManager.getLogger(this.getClass());
	@Autowired
	private CustomUserDetailService customUserDetailService;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			String token = jwtTokenProvider.getJwtFromRequest(request);

			if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
				Long userId = jwtTokenProvider.getUserIdFromJWT(token);

				// Loading the user details from db for check the user
				// authorities and password every time before allow the request
				UserDetails userDetails = customUserDetailService.loadUserById(userId);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);

			}
		} catch (UnauthorizedException ue) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.getWriter().print(ue.getMessage());
			return;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

}
