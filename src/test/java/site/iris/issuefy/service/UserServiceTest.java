package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.UserDto;
import site.iris.issuefy.repository.UserRepository;

class UserServiceTest {

	@InjectMocks
	UserService userService;

	@Mock
	UserRepository userRepository;

	@Mock
	GithubTokenService githubTokenService;

	private MockWebServer mockWebServer;

	@BeforeEach
	void setUp() throws IOException {
		MockitoAnnotations.openMocks(this);
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();
		userService = new UserService(userRepository, githubTokenService, webClient);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@DisplayName("신규회원이 로그인을 시도할 경우 DB에 저장한다.")
	@Test
	void registerUserIfNotExist() {
		// given
		UserDto loginUserDto = new UserDto("dokkisan", "https://avatars.githubusercontent.com/u/117690393?v=4",
			"test@email.com", false);

		// when
		when(userRepository.findByGithubId(loginUserDto.getGithubId())).thenReturn(Optional.empty());
		userService.registerUserIfNotExist(loginUserDto);

		// then
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository, times(1)).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();
		assertEquals(loginUserDto.getGithubId(), savedUser.getGithubId());
	}

	@DisplayName("사용자 정보를 정상적으로 조회한다.")
	@Test
	void getUserInfo() {
		// given
		String githubId = "testUser";
		User user = new User(githubId, "test@example.com");
		user.updateAlertStatus(true);

		// when
		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
		UserDto result = userService.getUserInfo(githubId);

		// then
		assertEquals(githubId, result.getGithubId());
		assertEquals("test@example.com", result.getEmail());
		assertTrue(result.isAlertStatus());
	}

	@DisplayName("이메일을 성공적으로 업데이트한다.")
	@Test
	void updateEmail() {
		// given
		String githubId = "testUser";
		String newEmail = "new@example.com";
		User user = new User(githubId, "old@example.com");

		// when
		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
		userService.updateEmail(githubId, newEmail);

		// then
		assertEquals(newEmail, user.getEmail());
		verify(userRepository).save(user);
	}

	@DisplayName("알림 상태를 성공적으로 업데이트한다.")
	@Test
	void updateAlert() {
		// given
		String githubId = "testUser";
		User user = new User(githubId, "test@example.com");

		// when
		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
		userService.updateAlert(githubId, true);

		// then
		assertTrue(user.isAlertStatus());
		verify(userRepository).save(user);
	}

	@DisplayName("회원 탈퇴를 성공적으로 처리한다.")
	@Test
	void withdraw() throws InterruptedException {
		// given
		String githubId = "testUser";
		String accessToken = "testAccessToken";
		String clientId = "testClientId";
		String clientSecret = "testClientSecret";

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(204)
			.setHeader("Content-Type", "application/json"));

		ReflectionTestUtils.setField(userService, "clientId", clientId);
		ReflectionTestUtils.setField(userService, "clientSecret", clientSecret);

		// when
		when(githubTokenService.findAccessToken(githubId)).thenReturn(accessToken);
		userService.withdraw(githubId);

		// then
		verify(userRepository).deleteByGithubId(githubId);

		// GitHub API 호출 확인
		RecordedRequest recordedRequest = mockWebServer.takeRequest();
		assertEquals("DELETE", recordedRequest.getMethod());
		assertEquals("/applications/" + clientId + "/grant", recordedRequest.getPath());
		assertTrue(recordedRequest.getHeader("Authorization").startsWith("Basic"));
	}
}