package site.iris.issuefy.exception.resource;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
	private final HttpStatus status;
	private final String errorParameter;

	public ResourceNotFoundException(String message, HttpStatus status, String errorParameter) {
		super(message);
		this.status = status;
		this.errorParameter = errorParameter;
	}
}
