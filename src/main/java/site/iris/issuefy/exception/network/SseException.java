package site.iris.issuefy.exception.network;

import org.springframework.http.HttpStatus;

public class SseException extends NetworkException {
	public SseException(String message, HttpStatus status) {
		super(message, status);
	}
}
