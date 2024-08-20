package site.iris.issuefy.exception.security;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UnauthenticatedException extends AuthenticationException {

	public UnauthenticatedException(String message, HttpStatus status) {
		super(message, status);
	}

}
