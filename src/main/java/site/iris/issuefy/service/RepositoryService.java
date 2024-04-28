package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import site.iris.issuefy.dto.RepositoryResponse;
import site.iris.issuefy.vo.RepositoryVO;

@Service
public class RepositoryService {

	public List<RepositoryResponse> getSubscribedRepositories() {
		List<RepositoryResponse> repositoryResponses = new ArrayList<>();
		repositoryResponses.add(RepositoryResponse.from(new RepositoryVO("issuefy", "iris")));

		return repositoryResponses;
	}
}
