package site.iris.issuefy.exception.security;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
	private final HttpStatus status;

	public AuthenticationException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}
}
