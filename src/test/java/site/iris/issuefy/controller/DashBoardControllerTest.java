package site.iris.issuefy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import site.iris.issuefy.response.DashBoardResponse;
import site.iris.issuefy.service.DashBoardService;

@Slf4j
@ExtendWith(RestDocumentationExtension.class)
@WebFluxTest(controllers = DashBoardController.class)
@Import(DashBoardControllerTest.TestSecurityConfig.class)
@AutoConfigureRestDocs
class DashBoardControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private DashBoardService dashBoardService;

	@Test
	@DisplayName("대시보드에 필요한 랭크, 주간방문수, 리포지토리 추가 횟수를 반환한다.")
	void dashboard() {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(6);
		DashBoardResponse mockResponse = DashBoardResponse.of(startDate, endDate, "A", "10", "5");

		when(dashBoardService.getDashBoardFromLoki(anyString())).thenReturn(Mono.just(mockResponse));

		webTestClient.get().uri("/api/dashboard")
			.header("Authorization", "Bearer mockJwtToken")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.startDate").isEqualTo(startDate.toString())
			.jsonPath("$.endDate").isEqualTo(endDate.toString())
			.jsonPath("$.rank").isEqualTo("A")
			.jsonPath("$.visitCount").isEqualTo("10")
			.jsonPath("$.addRepositoryCount").isEqualTo("5");
	}

	static class TestSecurityConfig {
		@Bean
		public WebFilter mockJwtFilter() {
			return (ServerWebExchange exchange, WebFilterChain chain) -> {
				exchange.getAttributes().put("githubId", "testUser");
				return chain.filter(exchange);
			};
		}
	}
}