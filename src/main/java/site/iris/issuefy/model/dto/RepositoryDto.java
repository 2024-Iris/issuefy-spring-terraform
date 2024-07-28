package site.iris.issuefy.model.dto;

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

	public static RepositoryDto of(Long id, String name, boolean isStarred) {
		return new RepositoryDto(id, name, isStarred);
	}
}
