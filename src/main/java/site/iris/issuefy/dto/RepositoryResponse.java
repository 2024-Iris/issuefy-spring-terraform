package site.iris.issuefy.dto;

import lombok.Data;
import site.iris.issuefy.vo.RepoVO;

@Data
public class RepositoryResponse {
	private Long id;
	private String name;
	private String org;

	private RepositoryResponse(Long id, String name, String org) {
		this.id = id;
		this.name = name;
		this.org = org;
	}

	public static RepositoryResponse from(RepoVO repoVO) {
		return new RepositoryResponse(1L, repoVO.name(), repoVO.org());
	}
}
