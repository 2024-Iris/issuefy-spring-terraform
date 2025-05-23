package site.iris.issuefy.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class IssueUserDto {
	@JsonProperty("login")
	private String githubId;

	@JsonProperty("avatar_url")
	private String githubProfileImage;
}
