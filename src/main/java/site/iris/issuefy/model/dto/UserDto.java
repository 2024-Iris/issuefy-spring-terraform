package site.iris.issuefy.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class UserDto {
	@JsonProperty("login")
	private String githubId;

	@JsonProperty("avatar_url")
	private String githubProfileImage;

	private String email;

	private boolean alertStatus;

	public static UserDto of(String login, String avatar_url, String email, boolean alertStatus) {
		return new UserDto(login, avatar_url, email, alertStatus);
	}

	public static UserDto of(String login, String email, boolean alertStatus) {
		return new UserDto(login, "", email, alertStatus);
	}
}
