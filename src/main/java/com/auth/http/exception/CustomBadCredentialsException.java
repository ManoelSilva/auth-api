package com.auth.http.exception;

public class CustomBadCredentialsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomBadCredentialsException() {
	}

	public CustomBadCredentialsException(String message) {
		super(message);
	}
}
