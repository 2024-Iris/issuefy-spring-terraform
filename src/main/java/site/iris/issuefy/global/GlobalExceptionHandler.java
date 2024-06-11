package site.iris.issuefy.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.common.ErrorResponse;
import site.iris.issuefy.exception.code.ErrorCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception e) {
		log.error("Server Internal Exception", e);
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
}
