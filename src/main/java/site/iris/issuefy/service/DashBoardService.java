package site.iris.issuefy.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
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
		double score = Double.parseDouble(visitCount) * 0.3 + Double.parseDouble(addRepositoryCount) * 0.7;

		if (score < 5) {
			return "C";
		} else if (score > 5 && score < 10) {
			return "B";
		}
		return "A";
	}
}
