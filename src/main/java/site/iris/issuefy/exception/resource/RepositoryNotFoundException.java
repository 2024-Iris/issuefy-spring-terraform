package site.iris.issuefy.exception.resource;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class RepositoryNotFoundException extends ResourceNotFoundException {
	public RepositoryNotFoundException(String message, HttpStatus httpStatus, String errorParameter) {
		super(message, httpStatus, errorParameter);
	}
}
