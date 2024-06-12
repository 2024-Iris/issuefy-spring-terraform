package site.iris.issuefy.global;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientException;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.common.ErrorResponse;
import site.iris.issuefy.exception.code.ErrorCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	// TODO: 예외 발생 원인에 따라 ErrorCode message, status 전달
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
		log.info(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		return new ResponseEntity<>(errorResponse, ErrorCode.REQUIRED_KEYS_MISSING.getStatus());
	}

	@ExceptionHandler(WebClientException.class)
	public ResponseEntity<ErrorResponse> handleWebClientException(WebClientException e) {
		log.info(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		return new ResponseEntity<>(errorResponse, ErrorCode.NOT_EXIST_REPOSITORY.getStatus());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception e) {
		log.error(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		return new ResponseEntity<>(errorResponse, ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
	}
}
