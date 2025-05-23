package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
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
	@DisplayName("Loki에서 대시보드 정보를 정상적으로 가져오는지 테스트한다.")
	void getDashBoardFromLoki_ShouldReturnCorrectResponse() throws Exception {
		// Given
		LokiQueryVisitDto visitDto = new LokiQueryVisitDto();
		visitDto.setVisitCount("0");
		String visitJson = objectMapper.writeValueAsString(visitDto);

		LokiQueryAddRepositoryDto repoDto = new LokiQueryAddRepositoryDto();
		repoDto.setAddRepositoryCount("0");
		String repoJson = objectMapper.writeValueAsString(repoDto);

		mockWebServer.enqueue(new MockResponse()
			.setBody(visitJson)
			.addHeader("Content-Type", "application/json"));
		mockWebServer.enqueue(new MockResponse()
			.setBody(repoJson)
			.addHeader("Content-Type", "application/json"));

		// When
		Mono<DashBoardResponse> result = dashBoardService.getDashBoardFromLoki("testUser");

		// Then
		StepVerifier.create(result)
			.expectNextMatches(response -> {
				assertEquals("0", response.getVisitCount());
				assertEquals("0", response.getAddRepositoryCount());
				assertNotNull(response.getRank());
				LocalDate today = LocalDate.now();
				assertEquals(today.minusDays(6), response.getStartDate());
				assertEquals(today, response.getEndDate());
				return true;
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("Loki에서 빈 응답을 받았을 때 올바르게 처리하는지 테스트한다.")
	void getDashBoardFromLoki_ShouldHandleEmptyResponses() {
		// Given
		mockWebServer.enqueue(new MockResponse()
			.setBody("{}")
			.addHeader("Content-Type", "application/json"));
		mockWebServer.enqueue(new MockResponse()
			.setBody("{}")
			.addHeader("Content-Type", "application/json"));

		// When
		Mono<DashBoardResponse> result = dashBoardService.getDashBoardFromLoki("testUser");

		// Then
		StepVerifier.create(result)
			.expectNextMatches(response -> {
				assertEquals("0", response.getVisitCount());
				assertEquals("0", response.getAddRepositoryCount());
				assertNotNull(response.getRank());
				LocalDate today = LocalDate.now();
				assertEquals(today.minusDays(6), response.getStartDate());
				assertEquals(today, response.getEndDate());
				return true;
			})
			.verifyComplete();
	}

	@Test
	@DisplayName("Loki에서 오류 응답을 받았을 때 올바르게 처리하는지 테스트한다.")
	void getDashBoardFromLoki_ShouldHandleErrorResponses() {
		// Given
		mockWebServer.enqueue(new MockResponse().setResponseCode(500));
		mockWebServer.enqueue(new MockResponse().setResponseCode(500));

		// When
		Mono<DashBoardResponse> result = dashBoardService.getDashBoardFromLoki("testUser");

		// Then
		StepVerifier.create(result)
			.expectNextMatches(response -> {
				assertEquals("0", response.getVisitCount());
				assertEquals("0", response.getAddRepositoryCount());
				assertNotNull(response.getRank());
				LocalDate today = LocalDate.now();
				assertEquals(today.minusDays(6), response.getStartDate());
				assertEquals(today, response.getEndDate());
				return true;
			})
			.verifyComplete();
	}
}