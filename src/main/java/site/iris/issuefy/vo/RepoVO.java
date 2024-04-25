package site.iris.issuefy.vo;

import java.util.Objects;

public record RepoVO(String name, String org) {

	public RepoVO{
		Objects.requireNonNull(name, "name must be not null");
		Objects.requireNonNull(org, "org must be not null");
	}
}
