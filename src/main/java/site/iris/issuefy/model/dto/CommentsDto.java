package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CommentsDto {
	@JsonProperty("id")
	private Long id;

	@JsonProperty("user")
	private IssueUserDto issueUserDto;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updateAt;

	@JsonProperty("body")
	private String body;
}
