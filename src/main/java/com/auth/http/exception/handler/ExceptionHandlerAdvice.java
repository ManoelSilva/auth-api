package com.auth.http.exception.handler;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.auth.http.exception.CustomBadCredentialsException;
import com.auth.http.exception.EntityNotFoundException;
import com.auth.http.exception.ExistingEntityException;
import com.auth.http.exception.UnauthorizedException;
import com.auth.http.response.ExceptionResponse;
import com.auth.i18n.MessageByLocaleService;

@ControllerAdvice
public class ExceptionHandlerAdvice {

	private static final String INVALID_SESSION = "invalid.session";
	private static final String BAD_CREDENTIALS = "bad.credentials";
	private static final String FIELD_KEY = "field.key";
	@Autowired
	private MessageByLocaleService messageService;

	private ResponseEntity<ExceptionResponse> executeExceptionHandler(Throwable ex, HttpStatus status) {
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(ex.getMessage()), status);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex) {
		return executeExceptionHandler(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResponse> handleBindingErrors(MethodArgumentNotValidException ex) {
		return new ResponseEntity<ExceptionResponse>(
				new ExceptionResponse(ex.getBindingResult().getFieldErrors().stream()
						.map(error -> messageService.getMessage(FIELD_KEY) + ": " + error.getField() + " "
								+ error.getDefaultMessage())
						.collect(Collectors.joining(", "))),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ BadCredentialsException.class, CustomBadCredentialsException.class })
	public final ResponseEntity<ExceptionResponse> handleBadCredentialsException(Exception ex) {
		return executeExceptionHandler(new CustomBadCredentialsException(messageService.getMessage(BAD_CREDENTIALS)),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public final ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException ex) {
		return executeExceptionHandler(new Exception(messageService.getMessage(INVALID_SESSION)),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ExistingEntityException.class)
	public final ResponseEntity<ExceptionResponse> handleExistingEntityException(ExistingEntityException ex) {
		return executeExceptionHandler(ex, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public final ResponseEntity<ExceptionResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
		return executeExceptionHandler(ex, HttpStatus.NOT_FOUND);
	}

}
