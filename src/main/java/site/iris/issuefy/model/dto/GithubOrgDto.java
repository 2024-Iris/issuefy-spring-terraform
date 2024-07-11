package site.iris.issuefy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GithubOrgDto {
	private long id;
	private String login;

	@Override
	public String toString() {
		return "GithubOrgDto{" +
			"id=" + id +
			", name='" + login + '\'' +
			'}';
	}
}
