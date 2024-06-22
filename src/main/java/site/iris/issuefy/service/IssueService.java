package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.IssueLabel;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.exception.RepositoryNotFoundException;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.LabelRepository;
import site.iris.issuefy.repository.RepositoryRepository;

@Service
public class IssueService {

	private final WebClient webClient;
	private final GithubTokenService githubTokenService;
	private final IssueRepository issueRepository;
	private final RepositoryRepository repositoryRepository;
	private final LabelRepository labelRepository;
	private final IssueLabelRepository issueLabelRepository;

	public IssueService(@Qualifier("apiWebClient") WebClient webClient,
		GithubTokenService githubTokenService,
		IssueRepository issueRepository,
		RepositoryRepository repositoryRepository,
		LabelRepository labelRepository,
		IssueLabelRepository issueLabelRepository) {
		this.webClient = webClient;
		this.githubTokenService = githubTokenService;
		this.issueRepository = issueRepository;
		this.repositoryRepository = repositoryRepository;
		this.labelRepository = labelRepository;
		this.issueLabelRepository = issueLabelRepository;
	}

	public Iterable<Issue> saveIssuesByRepository(String orgName, String repoName, String githubId) {
		List<IssueDto> issueDtos = getOpenGoodFirstIssues(orgName, repoName, githubId);
		Optional<Repository> repositoryOptional = repositoryRepository.findByName(repoName);

		if (repositoryOptional.isEmpty()) {
			throw new RepositoryNotFoundException(ErrorCode.NOT_EXIST_REPOSITORY.getMessage() + repoName);
		}

		Repository repository = repositoryOptional.get();
		List<Issue> issues = new ArrayList<>();
		List<Label> allLabels = new ArrayList<>();
		List<IssueLabel> issueLabels = new ArrayList<>();

		issueDtos.forEach(dto -> {
			Issue issue = Issue.of(repository, dto.getTitle(), dto.getGithubIssueNumber());
			issues.add(issue);

			dto.getLabels().forEach(labelDto -> {
				Label label = findOrCreateLabel(labelDto.getName(), labelDto.getColor());
				allLabels.add(label);

				IssueLabel issueLabel = IssueLabel.of(issue, label);
				issueLabels.add(issueLabel);
			});
		});

		issueRepository.saveAll(issues);
		labelRepository.saveAll(allLabels);
		issueLabelRepository.saveAll(issueLabels);

		return issues;
	}

	private Label findOrCreateLabel(String name, String color) {
		return labelRepository.findByNameAndColor(name, color)
			.orElseGet(() -> {
				Label newLabel = Label.of(name, color);
				return labelRepository.save(newLabel);
			});
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
	}
}
