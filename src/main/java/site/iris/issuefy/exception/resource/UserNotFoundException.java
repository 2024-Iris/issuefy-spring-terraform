package site.iris.issuefy.exception.resource;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UserNotFoundException extends ResourceNotFoundException {
	private final String githubId;

	public UserNotFoundException(String message, HttpStatus status, String githubId) {
		super(message, status, "");
		this.githubId = githubId;
	}
}
