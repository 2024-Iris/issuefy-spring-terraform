package site.iris.issuefy.exception.resource;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class IssueNotFoundException extends ResourceNotFoundException {
	public IssueNotFoundException(String message, HttpStatus httpStatus, String errorParameter) {
		super(message, httpStatus, errorParameter);
	}
}
