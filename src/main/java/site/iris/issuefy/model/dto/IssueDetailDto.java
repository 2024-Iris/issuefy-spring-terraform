package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class IssueDetailDto {
	@JsonProperty("number")
	private Long id;

	@JsonProperty("title")
	private String title;

	@JsonProperty("state")
	private String state;

	@JsonProperty("user")
	private IssueUserDto issueUserDto;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

	@JsonProperty("body")
	private String body;

	@JsonProperty("labels")
	private List<LabelDto> labels;
}
