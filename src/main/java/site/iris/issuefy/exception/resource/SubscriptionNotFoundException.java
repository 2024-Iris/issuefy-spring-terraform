package site.iris.issuefy.exception.resource;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class SubscriptionNotFoundException extends ResourceNotFoundException {
	private final String githubId;
	private final Long ghRepoId;

	public SubscriptionNotFoundException(String message, HttpStatus httpStatus, String githubId,
		Long ghRepoId) {
		super(message, httpStatus, "");
		this.githubId = githubId;
		this.ghRepoId = ghRepoId;
	}
}
