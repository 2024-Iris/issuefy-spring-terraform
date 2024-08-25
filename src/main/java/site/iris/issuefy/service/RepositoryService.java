package site.iris.issuefy.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.exception.resource.RepositoryNotFoundException;
import site.iris.issuefy.exception.validation.EmptyBodyException;
import site.iris.issuefy.model.dto.GithubRepositoryDto;
import site.iris.issuefy.repository.RepositoryRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService {

	private final RepositoryRepository repositoryRepository;

	@Transactional
	public Repository saveRepository(ResponseEntity<GithubRepositoryDto> repositoryInfo, Org org) {
		GithubRepositoryDto repoDto = repositoryInfo.getBody();
		log.info("Saving new repository: {}", repoDto);
		if (repoDto == null || repositoryInfo.getBody() == null) {
			throw new EmptyBodyException(ErrorCode.REPOSITORY_BODY_EMPTY.getMessage(),
				ErrorCode.REPOSITORY_BODY_EMPTY.getStatus());
		}

		return repositoryRepository.findByGhRepoId(repoDto.getId())
			.orElseGet(() -> {
				log.info("Saving new repository: {}", repoDto);
				Repository newRepository = new Repository(org, repoDto.getName(), repoDto.getId(),
					repoDto.getUpdated_at());
				return repositoryRepository.save(newRepository);
			});
	}

	public Repository findRepositoryByName(String repositoryName) {
		return repositoryRepository.findByName(repositoryName)
			.orElseThrow(() -> new RepositoryNotFoundException(ErrorCode.NOT_EXIST_REPOSITORY.getMessage(),
				ErrorCode.NOT_EXIST_REPOSITORY.getStatus(), repositoryName));
	}
}
