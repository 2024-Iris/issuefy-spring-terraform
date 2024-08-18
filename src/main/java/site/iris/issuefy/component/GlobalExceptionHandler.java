package site.iris.issuefy.component;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.reactive.function.client.WebClientException;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.exception.github.GithubApiException;
import site.iris.issuefy.exception.network.NetworkException;
import site.iris.issuefy.exception.resource.ResourceNotFoundException;
import site.iris.issuefy.exception.validation.ValidationException;
import site.iris.issuefy.response.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
		return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IOException e) {
		return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	@ExceptionHandler(WebClientException.class)
	public ResponseEntity<ErrorResponse> handleWebClientException(WebClientException e) {
		return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleRepositoryNotFoundException(ResourceNotFoundException e) {
		return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
	}

	@ExceptionHandler(GithubApiException.class)
	public ResponseEntity<ErrorResponse> handleGithubApiException(GithubApiException e) {
		return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getGithubMessage()));
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
		return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
	}

	@ExceptionHandler(NetworkException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(NetworkException e) {
		return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception e) {
		return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatusCode status, String message) {
		ErrorResponse errorResponse = new ErrorResponse(message);
		return ResponseEntity.status(status).body(errorResponse);
	}

	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public ResponseEntity<ErrorResponse> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
		return ResponseEntity
			.status(HttpStatus.SERVICE_UNAVAILABLE)
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ErrorResponse("Server-Sent Events connection timed out. Please try reconnecting."));
	}
}
