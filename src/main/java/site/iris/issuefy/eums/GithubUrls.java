package site.iris.issuefy.eums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GithubUrls {
	ORG_REQUEST_URL("https://api.github.com/orgs/"),
	REPOSITORY_REQUEST_URL("https://api.github.com/repos/");

	final String url;
}
