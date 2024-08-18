package site.iris.issuefy.exception.validation;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
	private final HttpStatus status;

	public ValidationException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}
}
