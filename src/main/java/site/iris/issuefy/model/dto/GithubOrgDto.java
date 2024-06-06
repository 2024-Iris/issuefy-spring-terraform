package site.iris.issuefy.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GithubOrgDto {
	private long id;
	private String name;

	@Override
	public String toString() {
		return "GithubOrgDto{" +
			"id=" + id +
			", name='" + name + '\'' +
			'}';
	}
}
