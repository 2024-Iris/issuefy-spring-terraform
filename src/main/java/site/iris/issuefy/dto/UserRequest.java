package site.iris.issuefy.dto;

import lombok.Getter;

public class UserRequest {
	@Getter
	private long id;
	private String email;

	public void setEmail(String mail) {
	}
}
