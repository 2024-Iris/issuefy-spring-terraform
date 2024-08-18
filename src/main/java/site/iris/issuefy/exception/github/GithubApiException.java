package site.iris.issuefy.exception.github;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public class GithubApiException extends RuntimeException {
	private final HttpStatusCode statusCode;
	private final String githubMessage;

	public GithubApiException(HttpStatusCode statusCode, String githubMessage) {
		super(githubMessage);
		this.statusCode = statusCode;
		this.githubMessage = githubMessage;
	}
}
