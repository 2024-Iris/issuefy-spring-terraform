package site.iris.issuefy.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.common.ErrorResponse;
import site.iris.issuefy.exception.RepositoryNotFoundException;
import site.iris.issuefy.exception.code.ErrorCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
		log.info(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
		log.info(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(RepositoryNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleRepositoryNotFoundException(RepositoryNotFoundException e) {
		log.info(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		return ResponseEntity.status(ErrorCode.NOT_EXIST_REPOSITORY.getStatus()).body(errorResponse);
	}

	@ExceptionHandler(WebClientException.class)
	public ResponseEntity<ErrorResponse> handleWebClientException(WebClientException e) {
		log.info(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception e) {
		log.error(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(errorResponse);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
		log.warn("Requested resource not found: {}", ex.getResourcePath());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body("Requested resource not found");
	}
}
