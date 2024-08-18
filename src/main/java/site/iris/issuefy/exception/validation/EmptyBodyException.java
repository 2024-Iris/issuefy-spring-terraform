package site.iris.issuefy.exception.validation;

import org.springframework.http.HttpStatus;

public class EmptyBodyException extends ValidationException {
	public EmptyBodyException(String message, HttpStatus status) {
		super(message, status);
	}
}
