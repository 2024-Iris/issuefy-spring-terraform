package site.iris.issuefy.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.model.dto.GithubRepositoryDto;
import site.iris.issuefy.repository.RepositoryRepository;

@Service
@RequiredArgsConstructor
public class RepositoryService {
	private final RepositoryRepository repositoryRepository;

	@Transactional
	public Repository saveRepository(ResponseEntity<GithubRepositoryDto> repositoryInfo, Org org) {
		return Optional.ofNullable(repositoryInfo.getBody())
			.map(body -> repositoryRepository.findByGhRepoId(body.getId())
				.orElseGet(() -> {
					Repository newRepository = new Repository(org, body.getName(), body.getId());
					return repositoryRepository.save(newRepository);
				})
			)
			.orElseThrow(() -> new NullPointerException("Repository info body is null"));
	}
}
