package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class GithubAccessTokenServiceTest {
	private MockWebServer mockWebServer;

	@DisplayName("mockWebServer로 Github API의 응답을 mocking 합니다.")
	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("access_token=testToken&scope=&token_type=bearer")
			.addHeader("Content-Type", "application/json"));
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	@DisplayName("AuthenticationCode를 전송하면 AccessToken을 반환한다.")
	void getToken() {
		// given
		WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
		GithubAccessTokenService githubAccessTokenService = new GithubAccessTokenService(webClient);
		ReflectionTestUtils.setField(githubAccessTokenService, "clientId", "testClientId");
		ReflectionTestUtils.setField(githubAccessTokenService, "clientSecret", "testClientSecret");

		String authenticationCode = "testCode";
		String expectedResponse = "access_token=testToken&scope=&token_type=bearer";

		// when
		String result = githubAccessTokenService.getToken(authenticationCode);

		// then
		assertNotNull(result);
		assertEquals(result, expectedResponse);
	}
}
