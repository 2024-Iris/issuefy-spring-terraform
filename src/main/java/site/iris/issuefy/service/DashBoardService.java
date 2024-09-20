package site.iris.issuefy.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import site.iris.issuefy.eums.DashBoardRank;
import site.iris.issuefy.eums.LokiQuery;
import site.iris.issuefy.filter.JwtAuthenticationFilter;
import site.iris.issuefy.model.dto.LokiQueryAddRepositoryDto;
import site.iris.issuefy.model.dto.LokiQueryVisitDto;
import site.iris.issuefy.response.DashBoardResponse;

@Slf4j
@Service
public class DashBoardService {
	private static final int MAX_SCORE = 100;
	private static final double VISIT_WEIGHT = 0.35;
	private static final double REPOSITORY_WEIGHT = 0.65;
	private static final int MINUS_DAYS = 6;
	private final WebClient webClient;

	public DashBoardService(@Qualifier("lokiWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	public Mono<DashBoardResponse> getDashBoardFromLoki(String githubId) {
		LocalDate today = LocalDate.now();
		LocalDate startWeek = today.minusDays(MINUS_DAYS);

		Mono<String> visitCountMono = getNumberOfWeeklyVisit(githubId, startWeek, today)
			.map(LokiQueryVisitDto::getVisitCount)
			.defaultIfEmpty("0");

		Mono<String> addRepositoryCountMono = getNumberOfWeeklyRepositoryAdded(githubId, startWeek, today)
			.map(LokiQueryAddRepositoryDto::getAddRepositoryCount)
			.defaultIfEmpty("0");

		return Mono.zip(visitCountMono, addRepositoryCountMono)
			.map(tuple -> {
				String visitCount = tuple.getT1();
				String addRepositoryCount = tuple.getT2();
				String rank = calculateRank(visitCount, addRepositoryCount);
				return DashBoardResponse.of(startWeek, today, rank, visitCount, addRepositoryCount);
			});
	}

	private Mono<LokiQueryVisitDto> getNumberOfWeeklyVisit(String githubId, LocalDate startWeek, LocalDate endWeek) {
		String maskedGithubId = JwtAuthenticationFilter.maskId(githubId);
		String rawLokiQuery = String.format(LokiQuery.NUMBER_OF_WEEKLY_VISIT.getQuery(), maskedGithubId, startWeek,
			endWeek);
		return webClient.get()
			.uri(uriBuilder -> uriBuilder.path("/loki/api/v1/query")
				.queryParam("query", "{query}")
				.build(rawLokiQuery))
			.retrieve()
			.bodyToMono(LokiQueryVisitDto.class)
			.doOnError(e -> log.error("Error fetching weekly visit count: {}", e.getMessage()))
			.onErrorResume(e -> Mono.empty());
	}

	private Mono<LokiQueryAddRepositoryDto> getNumberOfWeeklyRepositoryAdded(String githubId, LocalDate startWeek,
		LocalDate endWeek) {
		String maskedGithubId = JwtAuthenticationFilter.maskId(githubId);
		String rawLokiQuery = String.format(LokiQuery.NUMBER_OF_WEEKLY_REPOSITORY_ADDED.getQuery(), maskedGithubId,
			startWeek, endWeek);
		return webClient.get()
			.uri(uriBuilder -> uriBuilder.path("/loki/api/v1/query")
				.queryParam("query", "{query}")
				.build(rawLokiQuery))
			.retrieve()
			.bodyToMono(LokiQueryAddRepositoryDto.class)
			.doOnError(e -> log.error("Error fetching weekly repository add count: {}", e.getMessage()))
			.onErrorResume(e -> Mono.empty());
	}

	private String calculateRank(String visitCount, String addRepositoryCount) {
		int score = calculateScore(visitCount, addRepositoryCount);
		return DashBoardRank.getRankLabel(score);
	}

	private int calculateScore(String visitCount, String addRepositoryCount) {
		double visits = Math.log(Double.parseDouble(visitCount) + 1) / Math.log(2);
		double addedRepos = Math.log(Double.parseDouble(addRepositoryCount) + 1) / Math.log(2);

		// 정규화 과정
		double normalizedVisits = Math.min(visits / 30, 1);
		double normalizedRepos = Math.min(addedRepos / 25, 1);

		double rawScore = (normalizedVisits * VISIT_WEIGHT + normalizedRepos * REPOSITORY_WEIGHT) * MAX_SCORE;
		return (int)Math.min(rawScore, MAX_SCORE);
	}
}