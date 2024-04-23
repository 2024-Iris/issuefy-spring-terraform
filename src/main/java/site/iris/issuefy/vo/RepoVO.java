package site.iris.issuefy.vo;

import java.util.Objects;

public record RepoVO(String org, String repoName) {

	public RepoVO{
		Objects.requireNonNull(org, "org must be not null");
		Objects.requireNonNull(repoName, "repoName must be not null");
	}
}
