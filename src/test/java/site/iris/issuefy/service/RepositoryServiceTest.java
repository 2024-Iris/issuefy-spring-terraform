package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.model.dto.GithubRepositoryDto;
import site.iris.issuefy.repository.RepositoryRepository;

class RepositoryServiceTest {

	@InjectMocks
	private RepositoryService repositoryService;

	@Mock
	private RepositoryRepository repositoryRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("repositoryInfo의 Body가 null인 경우 NullPointerException을 발생시킨다.")
	void testSaveRepository_WithNullBody() {
		ResponseEntity<GithubRepositoryDto> repositoryInfo = ResponseEntity.ok(null);
		Org org = new Org(1L, "testOrg", 123L);

		NullPointerException exception = assertThrows(NullPointerException.class, () -> repositoryService.saveRepository(repositoryInfo, org));

		assertEquals("Repository info body is null", exception.getMessage());
	}

	@Test
	@DisplayName("존재하는 리포지토리를 반환한다.")
	void testSaveRepository_WithExistingRepository() {
		GithubRepositoryDto githubRepositoryDto = new GithubRepositoryDto(1L, "existingRepo", LocalDateTime.now());

		Org org = new Org(1L, "testOrg", 123L);
		Repository existingRepository = new Repository(1L, org, "existingRepo", false, 1L);

		when(repositoryRepository.findByGhRepoId(1L)).thenReturn(Optional.of(existingRepository));

		ResponseEntity<GithubRepositoryDto> repositoryInfo = ResponseEntity.ok(githubRepositoryDto);
		Repository result = repositoryService.saveRepository(repositoryInfo, org);

		assertEquals(existingRepository, result);
		verify(repositoryRepository, times(1)).findByGhRepoId(1L);
		verify(repositoryRepository, times(0)).save(any(Repository.class));
	}

	@Test
	@DisplayName("새로운 리포지토리를 생성하여 저장한다.")
	void testSaveRepository_WithNewRepository() {
		GithubRepositoryDto githubRepositoryDto = new GithubRepositoryDto(1L, "newRepo", LocalDateTime.now());

		Org org = new Org(1L, "testOrg", 123L);

		when(repositoryRepository.findByGhRepoId(1L)).thenReturn(Optional.empty());

		Repository newRepository = new Repository(1L, org, "newRepo", false, 1L);
		when(repositoryRepository.save(any(Repository.class))).thenReturn(newRepository);

		ResponseEntity<GithubRepositoryDto> repositoryInfo = ResponseEntity.ok(githubRepositoryDto);
		Repository result = repositoryService.saveRepository(repositoryInfo, org);

		assertEquals(newRepository, result);
		verify(repositoryRepository, times(1)).findByGhRepoId(1L);
		verify(repositoryRepository, times(1)).save(any(Repository.class));
	}
}
