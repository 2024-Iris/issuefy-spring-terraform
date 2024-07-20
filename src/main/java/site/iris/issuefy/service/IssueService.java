package site.iris.issuefy.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.IssueLabel;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.exception.RepositoryNotFoundException;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.mapper.IssueMapper;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.model.dto.IssueSubscriptionDto;
import site.iris.issuefy.model.vo.IssueSubscriptionVo;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.response.IssueResponse;
import site.iris.issuefy.response.RepositoryIssuesResponse;

@Slf4j
@Service
public class IssueService {
	private final WebClient webClient;
	private final GithubTokenService githubTokenService;
	private final IssueRepository issueRepository;
	private final RepositoryRepository repositoryRepository;
	private final LabelService labelService;
	private final IssueLabelRepository issueLabelRepository;

	public IssueService(@Qualifier("apiWebClient") WebClient webClient, GithubTokenService githubTokenService,
		IssueRepository issueRepository, RepositoryRepository repositoryRepository, LabelService labelService,
		IssueLabelRepository issueLabelRepository) {
		this.webClient = webClient;
		this.githubTokenService = githubTokenService;
		this.issueRepository = issueRepository;
		this.repositoryRepository = repositoryRepository;
		this.labelService = labelService;
		this.issueLabelRepository = issueLabelRepository;
	}

	@Transactional
	public RepositoryIssuesResponse getRepositoryIssuesResponse(String orgName, String repoName, String githubId) {
		Optional<Repository> optionalRepository = repositoryRepository.findByName(repoName);
		Repository repository = new Repository();
		if (optionalRepository.isPresent()) {
			repository = optionalRepository.get();
			LocalDateTime now = LocalDateTime.now();

			// Repository가 저장된지 1분 이내면 오픈되어 있는 GFI 저장
			if (repository.getUpdatedAt() != null && ChronoUnit.MINUTES.between(repository.getUpdatedAt(), now) < 1) {
				return getInitialIssues(githubId, orgName, repoName);
			}
		}
		return fetchIssues(orgName, repoName, repository, githubId);
	}

	private RepositoryIssuesResponse fetchIssues(String orgName, String repoName, Repository repository,
		String githubId) {
		IssueSubscriptionVo issueSubscriptionVo = getIssueSubscriptionData(orgName, repository, repoName, githubId);

		issueSubscriptionVo.getOptionalIssueDtos().ifPresent(issueDtos -> {
			LocalDateTime latestCreatedAt = issueRepository.getLatestCreatedAtByRepository_Id(repository.getId());

			issueDtos.forEach(dto -> {
				if (dto.getCreatedAt().isAfter(latestCreatedAt)) {
					Issue issue = createIssuesByDto(repository, dto, issueSubscriptionVo.getAllLabels(),
						issueSubscriptionVo.getIssueLabels());
					issueSubscriptionVo.getUpdatedIssues().add(issue);
				}
			});
		});
		saveAllEntities(IssueMapper.INSTANCE.issueVoToIssueDto(issueSubscriptionVo));
		List<IssueResponse> allIssueResponses = convertToResponse(
			issueRepository.findAllByRepository_Id(repository.getId()));
		return new RepositoryIssuesResponse(repoName, allIssueResponses);
	}

	private RepositoryIssuesResponse getInitialIssues(String githubId, String orgName, String repoName) {
		Repository repository = findRepositoryByName(repoName);
		IssueSubscriptionVo issueSubscriptionVo = getIssueSubscriptionData(orgName, repository, repoName, githubId);

		issueSubscriptionVo.getOptionalIssueDtos().ifPresent(dtos -> dtos.forEach(dto -> {
			Issue issue = createIssuesByDto(repository, dto, issueSubscriptionVo.getAllLabels(),
				issueSubscriptionVo.getIssueLabels());
			issueSubscriptionVo.getUpdatedIssues().add(issue);
		}));
		saveAllEntities(IssueMapper.INSTANCE.issueVoToIssueDto(issueSubscriptionVo));

		return new RepositoryIssuesResponse(repository.getName(),
			convertToResponse(issueSubscriptionVo.getUpdatedIssues()));
	}

	private IssueSubscriptionVo getIssueSubscriptionData(String orgName, Repository repository, String repoName,
		String githubId) {
		Optional<List<IssueDto>> issueDtos = getOpenGoodFirstIssues(orgName, repoName, githubId);
		return new IssueSubscriptionVo(repository, issueDtos);
	}

	private Issue createIssuesByDto(Repository repository, IssueDto issueDto, List<Label> allLabels,
		List<IssueLabel> issueLabels) {
		Issue issue = Issue.of(repository, issueDto.getTitle(), issueDto.isStarred(), issueDto.isRead(),
			issueDto.getState(), issueDto.getCreatedAt(), issueDto.getUpdatedAt(), issueDto.getClosedAt(),
			issueDto.getGhIssueId(), issueLabels);

		issueDto.getLabels().forEach(labelDto -> {
			Label label = labelService.findOrCreateLabel(labelDto.getName(), labelDto.getColor());
			allLabels.add(label);

			IssueLabel issueLabel = IssueLabel.of(issue, label);
			issueLabels.add(issueLabel);
		});

		return issue;
	}

	private Repository findRepositoryByName(String repositoryName) {
		// TODO: 리포지토리 이름 변경 시 update 로직 구현 필요
		return repositoryRepository.findByName(repositoryName)
			.orElseThrow(
				() -> new RepositoryNotFoundException(ErrorCode.NOT_EXIST_REPOSITORY.getMessage() + repositoryName));
	}

	private List<IssueResponse> convertToResponse(List<Issue> issues) {
		return issues.stream().map(issue -> {
			Optional<List<Label>> optionalLabels = labelService.getLabelsByIssueId(issue.getId());

			return IssueResponse.of(issue.getId(), issue.getGhIssueId(), issue.getState(), issue.getTitle(),
				labelService.convertLabelsResponse(optionalLabels), issue.isRead(), issue.isStarred(),
				issue.getCreatedAt(), issue.getUpdatedAt(), issue.getClosedAt());
		}).toList();
	}

	private Optional<List<IssueDto>> getOpenGoodFirstIssues(String orgName, String repoName, String githubId) {
		String accessToken = githubTokenService.findAccessToken(githubId);
		return Optional.ofNullable(webClient.get()
			.uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}/issues")
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
			.block());
	}

	private void saveAllEntities(IssueSubscriptionDto issueSubscriptionDto) {
		issueRepository.saveAll(issueSubscriptionDto.getUpdatedIssues());
		labelService.saveAllLabels(issueSubscriptionDto.getAllLabels());
		issueLabelRepository.saveAll(issueSubscriptionDto.getIssueLabels());
	}
}
