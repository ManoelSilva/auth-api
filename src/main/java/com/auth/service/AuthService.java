package com.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth.config.security.JwtTokenProvider;
import com.auth.http.exception.CustomBadCredentialsException;
import com.auth.http.exception.ExistingEntityException;
import com.auth.http.exception.UnauthorizedException;
import com.auth.i18n.MessageByLocaleService;
import com.auth.repository.UserRepository;
import com.auth.repository.model.User;

@Service
public class AuthService {

	private static final String EXISTING_ENTITY_EXCEPTION = "existing.entity.exception";
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private PasswordEncoder bcrypt;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MessageByLocaleService messageService;

	public User doRegister(User user) throws ExistingEntityException {
		if (!isUserRegistered(user)) {
			String pass = user.getPassword();

			user.setPassword(bcrypt.encode(pass));
			this.setPhonesToUser(user);
			this.userRepository.save(user);

			user.setToken(this.generateToken(user.getEmail(), pass));
		} else {
			throw new ExistingEntityException(this.messageService.getMessage(EXISTING_ENTITY_EXCEPTION));
		}

		return this.userRepository.save(user);
	}

	public User doLogin(User user) throws CustomBadCredentialsException {
		if (this.isUserRegistered(user)) {
			String pass = user.getPassword();
			user = this.userRepository.findByEmail(user.getEmail()).get();
			user.setToken(this.generateToken(user.getEmail(), pass));
		} else {
			throw new CustomBadCredentialsException();
		}
		return this.userRepository.save(user);
	}

	public User getProfile(String token, Long id) throws UnauthorizedException {
		this.validateAuthorization(token, id);
		return this.userRepository.findById(id).get();
	}

	private String generateToken(String email, String password) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(email, password));

		String token = this.jwtTokenProvider.generateToken(authentication);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return token;
	}

	private void validateAuthorization(String token, Long id) throws UnauthorizedException {
		User user = userRepository.findById(id).get();
		if (!user.getToken().equals(token)) {
			throw new UnauthorizedException();
		}
	}

	private void setPhonesToUser(User user) {
		user.getPhones().forEach(phone -> phone.setUser(user));
	}

	private Boolean isUserRegistered(User user) {
		Boolean isValid = false;
		if (this.userRepository.findByEmail(user.getEmail()).isPresent()) {
			isValid = true;
		}
		return isValid;
	}

}
