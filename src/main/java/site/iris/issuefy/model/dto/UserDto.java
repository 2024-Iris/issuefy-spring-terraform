package site.iris.issuefy.model.dto;

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

	private String email;

	public static UserDto of(String login, String avatar_url, String email) {
		return new UserDto(login, avatar_url, email);
	}

	@Override
	public String toString() {
		return "UserDto{" +
			"githubId='" + githubId + '\'' +
			", githubProfileImage='" + githubProfileImage + '\'' +
			", email='" + email + '\'' +
			'}';
	}
}
