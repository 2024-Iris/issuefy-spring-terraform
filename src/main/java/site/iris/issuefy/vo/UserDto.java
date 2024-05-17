package site.iris.issuefy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDto {
	@JsonProperty("login")
	private String githubId;

	@JsonProperty("avatar_url")
	private String githubProfileImage;

	public static UserDto of(String login, String avatar_url) {
		return new UserDto(login, avatar_url);
	}

	@Override
	public String toString() {
		return "UserDto{" +
			"githubId='" + githubId + '\'' +
			", githubProfileImage='" + githubProfileImage + '\'' +
			'}';
	}
}
