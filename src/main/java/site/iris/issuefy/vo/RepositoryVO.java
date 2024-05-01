package site.iris.issuefy.vo;

import java.util.Objects;

public record RepositoryVO(String name, String org) {

	public RepositoryVO {
		Objects.requireNonNull(name, "name must be not null");
		Objects.requireNonNull(org, "org must be not null");
	}
}
