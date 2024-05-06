package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

// 미완성 테스트 수정 예정
@SpringBootTest
@ActiveProfiles("application-test")
class GithubAccessTokenServiceTest {

	private MockWebServer mockWebServer;

	private final GithubAccessTokenService githubAccessTokenService = mock(GithubAccessTokenService.class);

	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	void getTokenShouldReturnAccessToken() {
		// given
		String authenticationCode = "testCode";
		String expectedAccessToken = "testAccessToken";
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("{\"access_token\": \"" + expectedAccessToken + "\", \"token_type\": \"bearer\"}")
			.addHeader("Content-Type", "application/json"));

		when(githubAccessTokenService.getToken(authenticationCode)).thenReturn(
			"access_token=testToken&scope=&token_type=bearer");

		String actualAccessToken = githubAccessTokenService.getToken("dummyCode");

	}
}
