package com.auth.http.response;

public class ExceptionResponse extends Response {

	public ExceptionResponse(String message) {
//		this.success = false;
		this.message = message;
	}
	
}
