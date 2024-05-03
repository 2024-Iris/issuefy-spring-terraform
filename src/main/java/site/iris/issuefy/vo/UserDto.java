package site.iris.issuefy.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDto {
	private String login;
	private String avatar_url;

	public static UserDto of(String login, String avatar_url) {
		return new UserDto(login, avatar_url);
	}

	@Override
	public String toString() {
		return "UserDto{" +
			"login='" + login + '\'' +
			", avatar_url='" + avatar_url + '\'' +
			'}';
	}
}
