package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class RepositoryDto {
	private Long id;
	private String name;
	private boolean isStarred;
	private LocalDateTime latestUpdateAt;

	public static RepositoryDto of(Long id, String name, boolean isStarred, LocalDateTime latestUpdateAt) {
		return new RepositoryDto(id, name, isStarred, latestUpdateAt);
	}
}
