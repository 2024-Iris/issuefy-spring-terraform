package site.iris.issuefy.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.exception.RepositoryNotFoundException;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.RepositoryRepository;

@Service
public class IssueService {

	private final WebClient webClient;
	private final GithubTokenService githubTokenService;
	private final IssueRepository issueRepository;
	private final RepositoryRepository repositoryRepository;

	public IssueService(@Qualifier("apiWebClient") WebClient webClient,
		GithubTokenService githubTokenService, IssueRepository issueRepository,
		RepositoryRepository repositoryRepository) {
		this.webClient = webClient;
		this.githubTokenService = githubTokenService;
		this.issueRepository = issueRepository;
		this.repositoryRepository = repositoryRepository;
	}

	public Iterable<Issue> saveIssuesByRepository(String orgName, String repoName, String githubId) {
		List<IssueDto> issueDtos = getOpenGoodFirstIssues(orgName, repoName, githubId);
		Optional<Repository> repository = repositoryRepository.findByName(repoName);
		if (repository.isPresent()) {
			List<Issue> issues = issueDtos.stream()
				.map(dto -> {
					List<Label> labels = dto.getLabels().stream()
						.map(labelDto -> Label.from(labelDto.getName()))
						.collect(Collectors.toList());
					return Issue.of(repository.get(), dto.getTitle(), dto.getGithubIssueNumber(), labels);
				})
				.collect(Collectors.toList());
			return issueRepository.saveAll(issues);
		}
		throw new RepositoryNotFoundException(ErrorCode.NOT_EXIST_REPOSITORY.getMessage() + repoName);
	}

	private List<IssueDto> getOpenGoodFirstIssues(String orgName, String repoName, String githubId) {
		String accessToken = githubTokenService.findAccessToken(githubId);
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/repos/{owner}/{repo}/issues")
				.queryParam("state", "open")
				.queryParam("sort", "created")
				.queryParam("direction", "desc")
				.queryParam("labels", "good first issue")
				.build(orgName, repoName))
			.header("accept", "application/vnd.github+json")
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.bodyToFlux(IssueDto.class)
			.collectList()
			.block();
		// return webClient.get()
		// 	.uri(uriBuilder -> uriBuilder
		// 		.path("repos/" + orgName + "/" + repoName + "/issues")
		// 		.queryParam("state", "open")
		// 		.queryParam("sort", "created")
		// 		.queryParam("direction", "desc")
		// 		.queryParam("labels", "good first issue")
		// 		.build("ownerValue", "repoValue")
		// 	)
		// 	.header("accept", "application/vnd.github+json")
		// 	.header("auth", accessToken)
		// 	.retrieve()
		// 	.bodyToFlux(IssueDto.class)
		// 	.collectList()
		// 	.block();
	}
}
