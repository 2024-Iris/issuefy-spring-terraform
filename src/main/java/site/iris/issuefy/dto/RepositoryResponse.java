package site.iris.issuefy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.iris.issuefy.vo.RepositoryVO;

@Data
@AllArgsConstructor
public class RepositoryResponse {
	private Long id;
	private String name;
	private String org;

	public static RepositoryResponse from(RepositoryVO repositoryVO) {
		return new RepositoryResponse(1L, repositoryVO.name(), repositoryVO.org());
	}
}
