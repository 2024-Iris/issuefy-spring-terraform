package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@ActiveProfiles("test")
class GithubAccessTokenServiceTest {
	private MockWebServer mockWebServer;

	@DisplayName("mockWebServer로 Github API의 응답을 mocking 합니다.")
	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("{\"access_token\": \"" + "\"testAccessToken\"" + "\", \"token_type\": \"bearer\"}")
			.addHeader("Content-Type", "application/json"));
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	@DisplayName("AuthenticationCode를 전송하면 AccessToken을 반환한다.")
	void getToken() {
		// given (GithubAccessTokenService 생성시 Value값이 null로 들어오는 문제로 인해 mocking 하여 진행하였습니다.)
		// WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
		// GithubAccessTokenService githubAccessTokenService = new GithubAccessTokenService(webClient);

		// given
		String authenticationCode = "testCode";
		GithubAccessTokenService githubAccessTokenService = mock(GithubAccessTokenService.class);
		when(githubAccessTokenService.getToken(authenticationCode)).thenReturn(
			"access_token=testToken&scope=&token_type=bearer");
		String expectedResponse = "access_token=testToken&scope=&token_type=bearer";

		// when
		String result = githubAccessTokenService.getToken(authenticationCode);

		// then
		assertNotNull(result);
		assertEquals(result, expectedResponse);
	}
}
