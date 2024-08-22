package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.transaction.Transactional;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.IssueLabel;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.exception.github.GithubApiException;
import site.iris.issuefy.exception.resource.IssueNotFoundException;
import site.iris.issuefy.exception.resource.RepositoryNotFoundException;
import site.iris.issuefy.exception.resource.UserNotFoundException;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.model.dto.IssueWithStarStatusDto;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.response.IssueResponse;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;

@Service
public class IssueService {
	private static final ErrorCode ISSUE_NOT_FOUND_ERROR = ErrorCode.NOT_EXIST_ISSUE;
	private final WebClient webClient;
	private final GithubTokenService githubTokenService;
	private final IssueRepository issueRepository;
	private final RepositoryRepository repositoryRepository;
	private final LabelService labelService;
	private final IssueLabelRepository issueLabelRepository;
	private final UserRepository userRepository;

	public IssueService(@Qualifier("apiWebClient") WebClient webClient, GithubTokenService githubTokenService,
		IssueRepository issueRepository, RepositoryRepository repositoryRepository, LabelService labelService,
		IssueLabelRepository issueLabelRepository, UserRepository userRepository) {
		this.webClient = webClient;
		this.githubTokenService = githubTokenService;
		this.issueRepository = issueRepository;
		this.repositoryRepository = repositoryRepository;
		this.labelService = labelService;
		this.issueLabelRepository = issueLabelRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public PagedRepositoryIssuesResponse getRepositoryIssues(String orgName, String repoName, String githubId, int page,
		int pageSize, String sort, String order) {
		Repository repository = findRepositoryByName(repoName);
		synchronizeRepositoryIssues(repository, orgName, repoName, githubId);
		return createPagedRepositoryIssuesResponse(repository, sort, order, githubId, page, pageSize);
	}

	// TODO Repository 서비스 분리 필요
	private Repository findRepositoryByName(String repositoryName) {
		return repositoryRepository.findByName(repositoryName)
			.orElseThrow(() -> new RepositoryNotFoundException(ErrorCode.NOT_EXIST_REPOSITORY.getMessage(),
				ErrorCode.NOT_EXIST_REPOSITORY.getStatus(), repositoryName));
	}

	@Transactional
	public void synchronizeRepositoryIssues(Repository repository, String orgName, String repoName, String githubId) {
		boolean issuesExist = issueRepository.existsById(repository.getId());

		if (!issuesExist) {
			addNewIssues(orgName, repoName, githubId, repository);
		} else {
			updateExistingIssues(orgName, repoName, githubId, repository);
		}
	}

	private void addNewIssues(String orgName, String repoName, String githubId, Repository repository) {
		List<IssueDto> githubIssues = fetchOpenGoodFirstIssuesFromGithub(orgName, repoName, githubId);
		List<Label> allLabels = new ArrayList<>();
		List<IssueLabel> issueLabels = new ArrayList<>();

		List<Issue> issues = githubIssues.stream()
			.map(dto -> createIssueEntityFromDto(repository, dto, allLabels, issueLabels))
			.collect(Collectors.toList());

		saveIssuesToDatabase(issues, issueLabels, allLabels);
	}

	private List<IssueDto> fetchOpenGoodFirstIssuesFromGithub(String orgName, String repoName, String githubId) {
		String accessToken = githubTokenService.findAccessToken(githubId);
		try {
			return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}/issues")
					.queryParam("state", "open")
					.queryParam("sort", "updated")
					.queryParam("direction", "desc")
					.queryParam("labels", "good first issue")
					.build(orgName, repoName))
				.header("accept", "application/vnd.github+json")
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.bodyToFlux(IssueDto.class)
				.collectList()
				.block();
		} catch (WebClientResponseException e) {
			throw new GithubApiException(e.getStatusCode(), e.getResponseBodyAsString());
		}
	}

	private Issue createIssueEntityFromDto(Repository repository, IssueDto issueDto, List<Label> allLabels,
		List<IssueLabel> issueLabels) {
		Issue issue = Issue.of(repository, issueDto.getTitle(), issueDto.isRead(), issueDto.getState(),
			issueDto.getCreatedAt(), issueDto.getUpdatedAt(), issueDto.getClosedAt(), issueDto.getGhIssueId(),
			issueLabels);

		issueDto.getLabels().forEach(labelDto -> {
			Label label = labelService.findOrCreateLabel(labelDto.getName(), labelDto.getColor());
			allLabels.add(label);

			IssueLabel issueLabel = IssueLabel.of(issue, label);
			issueLabels.add(issueLabel);
		});

		return issue;
	}

	private void saveIssuesToDatabase(List<Issue> issues, List<IssueLabel> issueLabels, List<Label> allLabels) {
		issueRepository.saveAll(issues);
		labelService.saveAllLabels(allLabels);
		issueLabelRepository.saveAll(issueLabels);
	}

	@Transactional
	public void updateExistingIssues(String orgName, String repoName, String githubId, Repository repository) {
		List<IssueDto> githubIssues = fetchOpenGoodFirstIssuesFromGithub(orgName, repoName, githubId);
		Issue mostRecentLocalIssue = findMostRecentLocalIssue(repository.getId());

		boolean needUpdateIssue = shouldUpdateIssues(githubIssues, mostRecentLocalIssue);

		if (needUpdateIssue) {
			updateLocalIssuesWithGithubData(githubIssues, repository);
		}
	}

	private Issue findMostRecentLocalIssue(Long repositoryId) {
		return issueRepository.findFirstByRepositoryIdOrderByUpdatedAtDesc(repositoryId)
			.orElseThrow(
				() -> new IssueNotFoundException(ISSUE_NOT_FOUND_ERROR.getMessage(), ISSUE_NOT_FOUND_ERROR.getStatus(),
					repositoryId.toString()));
	}

	private boolean shouldUpdateIssues(List<IssueDto> githubIssues, Issue mostRecentLocalIssue) {
		return githubIssues.stream()
			.findFirst()
			.map(firstDto -> isIssueUpdateRequired(firstDto, mostRecentLocalIssue))
			.orElse(false);
	}

	private boolean isIssueUpdateRequired(IssueDto latestGithubIssue, Issue latestDbIssue) {
		boolean isGithubIssueNewer = latestGithubIssue.getUpdatedAt().isAfter(latestDbIssue.getUpdatedAt());
		boolean isSameIssue = latestGithubIssue.getGhIssueId().equals(latestDbIssue.getGhIssueId());

		return isGithubIssueNewer && !isSameIssue;
	}

	// TODO 기존 이슈 삭제후 다시 인서트 방식으로 구현 벌크 업서트 방법을 도입하여 isRead 기능을 도입할지 재논의 필요
	//  레이블 기능 재검토 필요하여 레이블 기능 배제함
	@Transactional
	public void updateLocalIssuesWithGithubData(List<IssueDto> githubIssues, Repository repository) {
		List<Issue> updatedIssues = githubIssues.stream()
			.map(dto -> createIssueFromDto(dto, repository))
			.collect(Collectors.toList());

		issueRepository.saveAll(updatedIssues);
	}

	// TODO 현재 엔티티간 양방향 연관관계로 불변객체로 만들기가 어려움 엔티티 설계 재검토 필요
	//  현재 IssueLabel 엔티티에 issue 필드 Setter 주입하여 구현
	private Issue createIssueFromDto(IssueDto dto, Repository repository) {
		List<IssueLabel> issueLabels = dto.getLabels().stream().map(labelDto -> {
			Label label = labelService.findOrCreateLabel(labelDto.getName(), labelDto.getColor());
			return IssueLabel.of(null, label);
		}).toList();

		Issue issue = Issue.of(repository, dto.getTitle(), false, dto.getState(), dto.getCreatedAt(),
			dto.getUpdatedAt(), dto.getClosedAt(), dto.getGhIssueId(), issueLabels);

		issueLabels.forEach(issueLabel -> issueLabel.setIssue(issue));

		return issue;
	}

	// TODO userRepository User Service로 분리
	private PagedRepositoryIssuesResponse createPagedRepositoryIssuesResponse(Repository repository, String sort,
		String order, String githubId, int page, int pageSize) {
		Sort.Direction direction = Sort.Direction.fromString(order);
		Sort sorting = Sort.by(direction, sort);
		Pageable pageable = PageRequest.of(page, pageSize, sorting);

		ErrorCode userError = ErrorCode.NOT_EXIST_USER;
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserNotFoundException(userError.getMessage(), userError.getStatus(), githubId));
		Page<IssueWithStarStatusDto> issuePage = issueRepository.findIssuesWithStarStatus(repository.getId(),
			user.getId(), pageable);

		List<IssueResponse> issueResponseList = issuePage.getContent()
			.stream()
			.map(this::createIssueResponse)
			.toList();

		return PagedRepositoryIssuesResponse.of(
			issuePage.getNumber(),
			issuePage.getSize(),
			issuePage.getTotalElements(),
			issuePage.getTotalPages(),
			repository.getName(),
			issueResponseList
		);
	}

	private IssueResponse createIssueResponse(IssueWithStarStatusDto issueDto) {
		List<Label> labels = labelService.getLabelsByIssueId(issueDto.getIssue().getId());
		return IssueResponse.of(
			issueDto.getIssue().getId(),
			issueDto.getIssue().getGhIssueId(),
			issueDto.getIssue().getState(),
			issueDto.getIssue().getTitle(),
			labelService.convertLabelsResponse(labels),
			issueDto.getIssue().isRead(),
			issueDto.getIssue().getCreatedAt(),
			issueDto.getIssue().getUpdatedAt(),
			issueDto.getIssue().getClosedAt(),
			issueDto.isStarred()
		);
	}
}