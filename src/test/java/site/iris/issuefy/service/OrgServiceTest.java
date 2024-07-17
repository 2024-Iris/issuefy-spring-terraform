package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import site.iris.issuefy.entity.Org;
import site.iris.issuefy.model.dto.GithubOrgDto;
import site.iris.issuefy.repository.OrgRepository;

class OrgServiceTest {

	@Mock
	private OrgRepository orgRepository;

	@InjectMocks
	private OrgService orgService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("orgInfo의 Body가 null일 때 NullPointerException을 발생시킨다.")
	void testSaveOrg_WithNullBody() {
		ResponseEntity<GithubOrgDto> orgInfo = ResponseEntity.ok(null);

		NullPointerException exception = assertThrows(NullPointerException.class, () -> orgService.saveOrg(orgInfo));

		assertEquals("Org Info Body is null", exception.getMessage());
	}

	@Test
	@DisplayName("존재하는 Org를 반환한다.")
	void testSaveOrg_WithExistingOrg() {
		GithubOrgDto githubOrgDto = new GithubOrgDto(1L, "testOrg");

		Org existingOrg = new Org("testOrg", 12345L);
		when(orgRepository.findByName("testOrg")).thenReturn(Optional.of(existingOrg));

		ResponseEntity<GithubOrgDto> orgInfo = ResponseEntity.ok(githubOrgDto);
		Org result = orgService.saveOrg(orgInfo);

		assertEquals(existingOrg, result);
		verify(orgRepository, times(1)).findByName("testOrg");
		verify(orgRepository, times(0)).save(any(Org.class));
	}

	@Test
	@DisplayName("새로운 Org를 생성하여 저장한다.")
	void testSaveOrg_WithNewOrg() {
		GithubOrgDto githubOrgDto = new GithubOrgDto(1L, "testOrg");

		when(orgRepository.findByName("testOrg")).thenReturn(Optional.empty());

		Org newOrg = new Org("testOrg", 67890L);
		when(orgRepository.save(any(Org.class))).thenReturn(newOrg);

		ResponseEntity<GithubOrgDto> orgInfo = ResponseEntity.ok(githubOrgDto);
		Org result = orgService.saveOrg(orgInfo);

		assertEquals(newOrg, result);
		verify(orgRepository, times(1)).findByName("testOrg");
		verify(orgRepository, times(1)).save(any(Org.class));
	}
}