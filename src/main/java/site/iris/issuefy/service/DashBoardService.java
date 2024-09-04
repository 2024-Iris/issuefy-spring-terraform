package site.iris.issuefy.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.eums.DashBoardRank;
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.eums.LokiQuery;
import site.iris.issuefy.exception.network.LokiException;
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
	private final WebClient webClient;

	public DashBoardService(@Qualifier("lokiWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	public DashBoardResponse getDashBoardFromLoki(String githubId) {
		ErrorCode lokiError = ErrorCode.LOKI_EXCEPTION;
		String visitCount = getNumberOfWeeklyVisit(githubId, lokiError).getVisitCount();
		String addRepositoryCount = getNumberOfWeeklyRepositoryAdded(githubId, lokiError).getAddRepositoryCount();
		String rank = calculateRank(visitCount, addRepositoryCount);
		return DashBoardResponse.of(rank, visitCount, addRepositoryCount);
	}

	private LokiQueryVisitDto getNumberOfWeeklyVisit(String githubId, ErrorCode lokiError) {
		String maskGithubId = JwtAuthenticationFilter.maskId(githubId);
		String rawLokiQuery = String.format(LokiQuery.NUMBER_OF_WEEKLY_VISIT.getQuery(), maskGithubId);
		try {
			return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/loki/api/v1/query")
					.queryParam("query", "{query}")
					.build(rawLokiQuery))
				.retrieve()
				.bodyToMono(LokiQueryVisitDto.class)
				.block();
		} catch (Exception e) {
			throw new LokiException(lokiError.getMessage(), lokiError.getStatus());
		}
	}

	private LokiQueryAddRepositoryDto getNumberOfWeeklyRepositoryAdded(String githubId, ErrorCode lokiError) {
		String maskGithubId = JwtAuthenticationFilter.maskId(githubId);
		String rawLokiQuery = String.format(LokiQuery.NUMBER_OF_WEEKLY_REPOSITORY_ADDED.getQuery(), maskGithubId);
		try {
			return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/loki/api/v1/query")
					.queryParam("query", "{query}")
					.build(rawLokiQuery))
				.retrieve()
				.bodyToMono(LokiQueryAddRepositoryDto.class)
				.block();
		} catch (Exception e) {
			throw new LokiException(lokiError.getMessage(), lokiError.getStatus());
		}
	}

	private String calculateRank(String visitCount, String addRepositoryCount) {
		int score = calculateScore(visitCount, addRepositoryCount);
		return DashBoardRank.getRankLabel(score);
	}

	private int calculateScore(String visitCount, String addRepositoryCount) {
		double visits = Math.log(Double.parseDouble(visitCount) + 1) / Math.log(2);
		double addedRepos = Math.log(Double.parseDouble(addRepositoryCount) + 1) / Math.log(2);

		// 정규화 과정
		double normalizedVisits = Math.min(visits / 10, 1);
		double normalizedRepos = Math.min(addedRepos / 7, 1);

		double rawScore = (normalizedVisits * VISIT_WEIGHT + normalizedRepos * REPOSITORY_WEIGHT) * MAX_SCORE;
		return (int)Math.min(rawScore, MAX_SCORE);
	}
}
