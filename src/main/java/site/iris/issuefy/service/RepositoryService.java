package site.iris.issuefy.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.exception.validation.EmptyBodyException;
import site.iris.issuefy.model.dto.GithubRepositoryDto;
import site.iris.issuefy.repository.RepositoryRepository;

@Service
@RequiredArgsConstructor
public class RepositoryService {

	private final RepositoryRepository repositoryRepository;

	@Transactional
	public Repository saveRepository(ResponseEntity<GithubRepositoryDto> repositoryInfo, Org org) {
		if (repositoryInfo.getBody() == null) {
			throw new EmptyBodyException(ErrorCode.REPOSITORY_BODY_EMPTY.getMessage(),
				ErrorCode.REPOSITORY_BODY_EMPTY.getStatus());
		}

		GithubRepositoryDto repoDto = repositoryInfo.getBody();
		return repositoryRepository.findByGhRepoId(repoDto.getId())
			.orElseGet(() -> {
				Repository newRepository = new Repository(org, repoDto.getName(), repoDto.getId());
				return repositoryRepository.save(newRepository);
			});
	}
}
