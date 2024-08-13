package site.iris.issuefy.exception.validation;

import org.springframework.http.HttpStatus;

public class InvalidUrlException extends ValidationException {
	public InvalidUrlException(String message, HttpStatus status) {
		super(message, status);
	}
}
