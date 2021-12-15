package com.example.demo.controller;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	public static final String ACCESS_DENIED = "Access denied!";
	public static final String INVALID_REQUEST = "Invalid request";
	public static final String ERROR_MESSAGE_TEMPLATE = "message: %s %n requested uri: %s";
	public static final String LIST_JOIN_DELIMITER = ",";
	public static final String FIELD_ERROR_SEPARATOR = ": ";
	private static final String ERRORS_FOR_PATH = "errors {} for path {}";
	private static final String PATH = "path";
	private static final String ERRORS = "error";
	private static final String STATUS = "status";
	private static final String MESSAGE = "message";
	private static final String TIMESTAMP = "timestamp";
	private static final String TYPE = "type";
	
	@ExceptionHandler({AccessDeniedException.class})
	public ResponseEntity<Object> accessDeniedException(AccessDeniedException exception, WebRequest request) {
		logger.info(exception.getMessage());
		return getExceptionResponseEntity(exception, HttpStatus.FORBIDDEN, request, Collections.singletonList("Access denied exception"));
	}
	
	@ExceptionHandler({SQLException.class})
	public ResponseEntity<Object> sqlError(SQLException exception) {
		logger.info(exception.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some SQL exception occurred");
	}
	
	@Override
	@NonNull
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception, @NonNull HttpHeaders headers,
			@NonNull HttpStatus status, @NonNull WebRequest request) {
		/*ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ObjectNode errors = mapper.createObjectNode();
		rootNode.set("Errors", errors);
		
		errors.put(field, message);
		
		List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
		for (ObjectError error : allErrors) {
			String field;
			if (error instanceof FieldError) {
				field = ((FieldError) error).getField();
			} else {
				field = Objects.requireNonNull(error.getArguments())[0].toString();
			}
			String errorMessage = error.getDefaultMessage();
			errorResponse.addError(field, errorMessage);
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse.getRootNode());*/
		List<String> validationErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + FIELD_ERROR_SEPARATOR + error.getDefaultMessage())
				.collect(Collectors.toList());
		return getExceptionResponseEntity(exception, HttpStatus.BAD_REQUEST, request, validationErrors);
	}
	
	@ExceptionHandler({ConstraintViolationException.class})
	public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException exception, WebRequest request) {
		List<String> validationErrors = exception.getConstraintViolations().stream()
				.map(violation -> violation.getPropertyPath() + FIELD_ERROR_SEPARATOR + violation.getMessage())
				.collect(Collectors.toList());
		return getExceptionResponseEntity(exception, HttpStatus.BAD_REQUEST, request, validationErrors);
	}
	
	@Override
	@NonNull
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			@NonNull HttpMessageNotReadableException exception, @NonNull HttpHeaders headers,
			@NonNull HttpStatus status, @NonNull WebRequest request) {
		return getExceptionResponseEntity(exception, status, request,
				Collections.singletonList(exception.getLocalizedMessage()));
	}
	
	/**
	 * A general handler for all uncaught exceptions
	 */
	@ExceptionHandler({Exception.class})
	public ResponseEntity<Object> handleAllExceptions(Exception exception, WebRequest request) {
		ResponseStatus responseStatus = exception.getClass().getAnnotation(ResponseStatus.class);
		HttpStatus status = responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
		String localizedMessage = exception.getLocalizedMessage();
		String path = request.getDescription(false);
		String message = (StringUtils.isNotEmpty(localizedMessage) ? localizedMessage : status.getReasonPhrase());
		logger.error(String.format(ERROR_MESSAGE_TEMPLATE, message, path), exception);
		return getExceptionResponseEntity(exception, status, request, Collections.singletonList(message));
	}
	
	/**
	 * Build detailed information about the exception in the response
	 */
	private ResponseEntity<Object> getExceptionResponseEntity(Exception exception,
	                                                          HttpStatus status,
	                                                          WebRequest request,
	                                                          List<String> errors) {
		Map<String, Object> body = new LinkedHashMap<>();
		String path = request.getDescription(false);
		body.put(TIMESTAMP, Instant.now());
		body.put(STATUS, status.value());
		body.put(ERRORS, errors);
		body.put(TYPE, exception.getClass().getSimpleName());
		body.put(PATH, path);
		body.put(MESSAGE, getMessageForStatus(status));
		String errorsMessage = CollectionUtils.isNotEmpty(errors) ?
				errors.stream()
						.filter(StringUtils::isNotEmpty)
						.collect(Collectors.joining(LIST_JOIN_DELIMITER))
				: status.getReasonPhrase();
		logger.error(ERRORS_FOR_PATH, errorsMessage, path);
		return new ResponseEntity<>(body, status);
	}
	
	private String getMessageForStatus(HttpStatus status) {
		switch (status) {
			case UNAUTHORIZED:
				return ACCESS_DENIED;
			case BAD_REQUEST:
				return INVALID_REQUEST;
			default:
				return status.getReasonPhrase();
		}
	}
}