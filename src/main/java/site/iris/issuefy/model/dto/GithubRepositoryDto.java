package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GithubRepositoryDto {
	private long id;
	private String name;
	private LocalDateTime updated_at;

	@Override
	public String toString() {
		return "GithubRepositoryDto{" +
			"id=" + id +
			", name='" + name + '\'' +
			", updated_at=" + updated_at +
			'}';
	}
}
