package site.iris.issuefy.model.dto;

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

	@Override
	public String toString() {
		return "RepositoryDto{" +
			"id=" + id +
			", name='" + name + '\'' +
			'}';
	}
}
