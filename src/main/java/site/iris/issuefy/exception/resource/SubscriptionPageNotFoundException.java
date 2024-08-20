package site.iris.issuefy.exception.resource;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class SubscriptionPageNotFoundException extends ResourceNotFoundException {
	private final String githubId;

	public SubscriptionPageNotFoundException(String message, HttpStatus httpStatus, String githubId) {
		super(message, httpStatus, "");
		this.githubId = githubId;
	}
}
