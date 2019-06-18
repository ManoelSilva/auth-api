package com.auth.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.i18n.MessageByLocaleService;
import com.auth.repository.UserRepository;
import com.auth.repository.model.User;

@Service
public class CustomUserDetailService implements UserDetailsService {

	private static final String USER_NOT_FOUND = "user.not.found";
	@Autowired
	protected MessageByLocaleService messageService;
	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {

		User user = this.userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException(messageService.getMessage(USER_NOT_FOUND)));

		return new UserPrincipal(user);
	}

	@Transactional(readOnly = true)
	public UserDetails loadUserById(Long id) {
		User user = this.userRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException(messageService.getMessage(USER_NOT_FOUND)));

		return new UserPrincipal(user);
	}
}
