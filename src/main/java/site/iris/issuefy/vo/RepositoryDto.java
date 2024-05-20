package site.iris.issuefy.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RepositoryDto {
	private Long id;
	private String name;

	public static RepositoryDto of(Long id, String name) {
		return new RepositoryDto(id, name);
	}
}
