package site.iris.issuefy.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IssueService {

	private final WebClient webClient;
	private final GithubTokenService githubTokenService;

	public IssueService(@Qualifier("apiWebClient") WebClient webClient,
		GithubTokenService githubTokenService) {
		this.webClient = webClient;
		this.githubTokenService = githubTokenService;
	}

	public String getIssuesByRepoName(String repoName) {
		String accessToken = githubTokenService.findAccessToken("lvalentine6");
		String responseBody = webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("repos/elastic/" + repoName + "/issues")
				.queryParam("state", "open")
				.queryParam("sort", "created")
				.queryParam("direction", "desc")
				.queryParam("labels", "good first issue")
				.build("ownerValue", "repoValue")
			)
			.header("accept", "application/vnd.github+json")
			.header("auth", accessToken)
			.retrieve()
			.bodyToMono(String.class)
			.block();

		log.info(responseBody);
		return responseBody;
	}
}
