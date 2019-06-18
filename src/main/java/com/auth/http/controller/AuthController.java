package com.auth.http.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.auth.config.security.JwtTokenProvider;
import com.auth.http.exception.CustomBadCredentialsException;
import com.auth.http.exception.ExistingEntityException;
import com.auth.http.exception.UnauthorizedException;
import com.auth.repository.model.User;
import com.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) throws CustomBadCredentialsException {
		return ResponseEntity.ok(this.authService.doLogin(user));
	}

	@PostMapping("/register")
	@ResponseBody
	public ResponseEntity<User> register(@RequestBody @Valid User user)
			throws ExistingEntityException, CustomBadCredentialsException {
		return ResponseEntity.ok(this.authService.doRegister(user));
	}

	@GetMapping("/user/{id}")
	@ResponseBody
	public ResponseEntity<User> profile(HttpServletRequest request, @PathVariable("id") Long id)
			throws UnauthorizedException {
		return ResponseEntity.ok(this.authService.getProfile(jwtTokenProvider.getJwtFromRequest(request), id));
	}

}
