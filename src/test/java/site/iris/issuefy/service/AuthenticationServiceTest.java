package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import site.iris.issuefy.repository.GithubTokenRepository;
import site.iris.issuefy.vo.UserDto;

class AuthenticationServiceTest {
	private MockWebServer mockWebServer;

	@BeforeEach
	@DisplayName("mockWebServer로 Github API의 응답을 mocking 합니다.")
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockWebServer.enqueue(new MockResponse()
			.setBody("{\"login\": \"testUser\", \"avatar_url\": \"testUserUrl\"}")
			.addHeader("Content-Type", "application/json")
			.setResponseCode(200));
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	@DisplayName("authenticationCode로 로그인 유저 정보를 반환한다.")
	void githubLogin() {
		// given
		String authenticationCode = "testCode";
		UserDto userDto = UserDto.of("testUser", "testUserUrl");

		GithubAccessTokenService githubAccessTokenService = mock(GithubAccessTokenService.class);
		when(githubAccessTokenService.getToken(authenticationCode)).thenReturn(
			"access_token=testToken&scope=&token_type=bearer");

		WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
		GithubTokenRepository githubTokenRepository = mock(GithubTokenRepository.class);
		UserService userService = mock(UserService.class);
		AuthenticationService authenticationService = new AuthenticationService(githubAccessTokenService, webClient, userService, githubTokenRepository);

		// when
		UserDto result = authenticationService.githubLogin(authenticationCode);

		// then
		assertNotNull(result);
		assertEquals(userDto.getGithubId(), result.getGithubId());
		assertEquals(userDto.getGithubProfileImage(), result.getGithubProfileImage());
	}
}
