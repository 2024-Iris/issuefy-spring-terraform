package site.iris.issuefy.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import site.iris.issuefy.response.DashBoardResponse;

@ExtendWith(MockitoExtension.class)
class DashBoardServiceTest {

	@Mock
	private DashBoardService dashBoardService;

	private final LocalDate fixedDate = LocalDate.of(2024, 9, 16);

	@BeforeEach
	void setUp() {
	}

	@Test
	void getDashBoardFromLoki_ShouldReturnCorrectResponse() {
		// Given
		DashBoardResponse mockResponse = new DashBoardResponse(
			fixedDate.minusDays(6),
			fixedDate,
			"A",
			"10",
			"5"
		);
		when(dashBoardService.getDashBoardFromLoki(any())).thenReturn(Mono.just(mockResponse));

		// When & Then
		StepVerifier.create(dashBoardService.getDashBoardFromLoki("testUser"))
			.expectNext(mockResponse)
			.verifyComplete();
	}
}