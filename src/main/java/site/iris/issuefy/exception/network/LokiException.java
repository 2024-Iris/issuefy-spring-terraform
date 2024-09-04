package site.iris.issuefy.exception.network;

import org.springframework.http.HttpStatus;

public class LokiException extends NetworkException {
	public LokiException(String message, HttpStatus status) {
		super(message, status);
	}
}
