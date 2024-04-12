package site.iris.issuefy.domain;

import lombok.Getter;

public class User {
	@Getter
	private long id;
	private String email;

	public User(long id) {
		this.id = id;
	}
}
