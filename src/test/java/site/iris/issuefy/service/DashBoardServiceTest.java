package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.exception.network.LokiException;
import site.iris.issuefy.model.dto.LokiQueryAddRepositoryDto;
import site.iris.issuefy.model.dto.LokiQueryVisitDto;
import site.iris.issuefy.response.DashBoardResponse;

class DashBoardServiceTest {

	private static MockWebServer mockWebServer;
	private DashBoardService dashBoardService;
	private ObjectMapper objectMapper;

	@BeforeAll
	static void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@BeforeEach
	void initialize() {
		String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
		WebClient webClient = WebClient.create(baseUrl);
		dashBoardService = new DashBoardService(webClient);
		objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("Loki로부터 랭크, 주간방문수, 리포지토리 추가 횟수를 반환한다.")
	void getDashBoardFromLoki_ShouldReturnCorrectResponse() throws Exception {
		// Given
		LokiQueryVisitDto visitDto = new LokiQueryVisitDto();
		visitDto.setVisitCount("10");
		String visitJson = objectMapper.writeValueAsString(visitDto);

		LokiQueryAddRepositoryDto repoDto = new LokiQueryAddRepositoryDto();
		repoDto.setAddRepositoryCount("5");
		String repoJson = objectMapper.writeValueAsString(repoDto);

		mockWebServer.enqueue(new MockResponse()
			.setBody(visitJson)
			.addHeader("Content-Type", "application/json"));
		mockWebServer.enqueue(new MockResponse()
			.setBody(repoJson)
			.addHeader("Content-Type", "application/json"));

		// When
		DashBoardResponse response = dashBoardService.getDashBoardFromLoki("testUser");

		// Then
		assertNotNull(response);
		assertEquals("10", response.getVisitCount());
		assertEquals("5", response.getAddRepositoryCount());
		assertNotNull(response.getRank());
	}

	@Test
	@DisplayName("Loki 쿼리 실패 시 LokiException 발생한다.")
	void getDashBoardFromLoki_ShouldThrowLokiException_WhenQueryFails() {
		// Given
		mockWebServer.enqueue(new MockResponse().setResponseCode(500));

		// When & Then
		ErrorCode expectedError = ErrorCode.LOKI_EXCEPTION;
		LokiException exception = assertThrows(LokiException.class,
			() -> dashBoardService.getDashBoardFromLoki("testUser"));

		assertEquals(expectedError.getMessage(), exception.getMessage());
		assertEquals(expectedError.getStatus(), exception.getStatus());
	}
}