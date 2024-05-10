package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.dto.IssueResponse;

@Slf4j
@Service
public class IssueService {

	public List<IssueResponse> getIssuesByRepoName(String repoName) {
		IssueResponse issueResponse = IssueResponse.of(1L, 1, "CI/CD", "deploy");
		List<IssueResponse> issueResponses = new ArrayList<>();
		issueResponses.add(issueResponse);

		// 아직 DB 작업이 완료되지 않아 엑세스 토큰으로 테스트 요청을 만들었습니다.
		WebClient test = WebClient.create("https://api.github.com");
		String responseBody = test.get()
			.uri(uriBuilder -> uriBuilder
				.path("repos/elastic/elasticsearch/issues")
				.queryParam("state", "open")
				.queryParam("sort", "created")
				.queryParam("direction", "desc")
				.queryParam("labels", "good first issue")
				.build("ownerValue", "repoValue")
			)
			.header("accept", "application/vnd.github+json")
			// .header("auth", oauthDto.getAccess_token())
			.retrieve()
			.bodyToMono(String.class)
			.block();

		log.info(responseBody);

		return issueResponses;
	}
}
