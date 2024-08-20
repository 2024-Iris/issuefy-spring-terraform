package site.iris.issuefy.exception.network;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class NetworkException extends RuntimeException {
	private final HttpStatus status;

	public NetworkException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}
}
