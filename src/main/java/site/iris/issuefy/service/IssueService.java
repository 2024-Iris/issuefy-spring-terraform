package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.IssueLabel;
import site.iris.issuefy.entity.IssueStar;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.exception.github.GithubApiException;
import site.iris.issuefy.exception.resource.IssueNotFoundException;
import site.iris.issuefy.mapper.IssueMapper;
import site.iris.issuefy.model.dto.CommentsDto;
import site.iris.issuefy.model.dto.IssueDetailDto;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.model.dto.IssueWithPagedDto;
import site.iris.issuefy.model.dto.IssueWithStarDto;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.IssueStarRepository;
import site.iris.issuefy.response.IssueDetailAndCommentsResponse;
import site.iris.issuefy.response.IssueResponse;
import site.iris.issuefy.response.IssueStarResponse;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;
import site.iris.issuefy.response.StarRepositoryIssuesResponse;

@Slf4j
@Service
public class IssueService {
	private static final ErrorCode ISSUE_NOT_FOUND_ERROR = ErrorCode.NOT_EXIST_ISSUE;
	private static final int ISSUE_STAR_SIZE = 5;
	private final WebClient webClient;
	private final GithubTokenService githubTokenService;
	private final IssueRepository issueRepository;
	private final RepositoryService repositoryService;
	private final LabelService labelService;
	private final IssueLabelRepository issueLabelRepository;
	private final UserService userService;
	private final IssueStarRepository issueStarRepository;

	public IssueService(@Qualifier("apiWebClient") WebClient webClient, GithubTokenService githubTokenService,
		IssueRepository issueRepository, RepositoryService repositoryService, LabelService labelService,
		IssueLabelRepository issueLabelRepository, UserService userService, IssueStarRepository issueStarRepository) {
		this.webClient = webClient;
		this.githubTokenService = githubTokenService;
		this.issueRepository = issueRepository;
		this.repositoryService = repositoryService;
		this.labelService = labelService;
		this.issueLabelRepository = issueLabelRepository;
		this.userService = userService;
		this.issueStarRepository = issueStarRepository;
	}

	@Transactional
	public PagedRepositoryIssuesResponse getRepositoryIssues(String orgName, String repoName, String githubId, int page,
		int pageSize, String sort, String order) {
		Repository repository = repositoryService.findRepositoryByName(repoName);
		synchronizeRepositoryIssues(repository, orgName, repoName, githubId);
		return getPagedRepositoryIssuesResponse(repository, sort, order, githubId, page, pageSize);
	}

	@Transactional
	public void synchronizeRepositoryIssues(Repository repository, String orgName, String repoName, String githubId) {
		boolean issuesExist = issueRepository.existsById(repository.getId());

		if (!issuesExist) {
			addNewIssues(orgName, repoName, githubId, repository);
			return;
		}

		updateExistingIssues(orgName, repoName, githubId, repository);
	}

	private void addNewIssues(String orgName, String repoName, String githubId, Repository repository) {
		List<IssueDto> githubIssues = fetchOpenGoodFirstIssuesFromGithub(orgName, repoName, githubId).orElseThrow(
			() -> new GithubApiException(ErrorCode.GITHUB_RESPONSE_BODY_EMPTY.getStatus(),
				ErrorCode.GITHUB_RESPONSE_BODY_EMPTY.getMessage()));

		List<Label> allLabels = new ArrayList<>();
		List<IssueLabel> issueLabels = new ArrayList<>();

		List<Issue> issues = githubIssues.stream()
			.map(dto -> createIssueEntityFromDto(repository, dto, allLabels, issueLabels))
			.toList();

		issueRepository.saveAll(issues);
		labelService.saveAllLabels(allLabels);
		issueLabelRepository.saveAll(issueLabels);
	}

	private Optional<List<IssueDto>> fetchOpenGoodFirstIssuesFromGithub(String orgName, String repoName,
		String githubId) {
		String accessToken = githubTokenService.findAccessToken(githubId);
		try {
			List<IssueDto> issues = webClient.get()
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

			return Optional.ofNullable(issues);
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

	@Transactional
	public void updateExistingIssues(String orgName, String repoName, String githubId, Repository repository) {
		List<IssueDto> githubIssues = fetchOpenGoodFirstIssuesFromGithub(orgName, repoName, githubId).orElseThrow(
			() -> new GithubApiException(ErrorCode.GITHUB_RESPONSE_BODY_EMPTY.getStatus(),
				ErrorCode.GITHUB_RESPONSE_BODY_EMPTY.getMessage()));

		Issue mostRecentLocalIssue = findMostRecentLocalIssue(repository.getId());
		boolean needUpdateIssue = shouldUpdateIssues(githubIssues, mostRecentLocalIssue);

		if (needUpdateIssue) {
			synchronizeIssuesWithGithub(githubIssues, repository);
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
			.map(firstDto -> isGithubIssueNewer(firstDto, mostRecentLocalIssue))
			.orElse(false);
	}

	private boolean isGithubIssueNewer(IssueDto latestGithubIssue, Issue latestDbIssue) {
		boolean isGithubIssueNewer = latestGithubIssue.getUpdatedAt().isAfter(latestDbIssue.getUpdatedAt());
		boolean isSameIssue = latestGithubIssue.getGhIssueId().equals(latestDbIssue.getGhIssueId());

		return isGithubIssueNewer && !isSameIssue;
	}

	@Transactional
	public void synchronizeIssuesWithGithub(List<IssueDto> githubIssues, Repository repository) {
		List<Issue> newIssues = new ArrayList<>();
		List<Issue> updatedIssues = new ArrayList<>();
		Set<Long> existingIssueIds = new HashSet<>(issueRepository.findGhIssueIdByRepositoryId(repository.getId()));
		Set<IssueLabel> allIssueLabels = new HashSet<>();

		githubIssues.forEach(githubIssue -> {
			if (existingIssueIds.contains(githubIssue.getGhIssueId())) {
				updatedIssues.add(updateExistingIssue(githubIssue));
				return;
			}
			newIssues.add(createNewIssueFromDto(githubIssue, repository));
		});

		issueRepository.saveAll(newIssues);
		allIssueLabels.addAll(collectUniqueIssueLabels(newIssues));
		allIssueLabels.addAll(collectUniqueIssueLabels(updatedIssues));
		issueLabelRepository.saveAll(allIssueLabels);
	}

	@Transactional
	public Issue updateExistingIssue(IssueDto dto) {
		ErrorCode issueError = ErrorCode.NOT_EXIST_ISSUE;
		Issue issue = issueRepository.findByGhIssueId(dto.getGhIssueId())
			.orElseThrow(() -> new IssueNotFoundException(issueError.getMessage(), issueError.getStatus(),
				String.valueOf(dto.getGhIssueId())));
		IssueMapper.INSTANCE.updateIssueFromDto(dto, issue);
		return issue;
	}

	private Issue createNewIssueFromDto(IssueDto dto, Repository repository) {
		List<IssueLabel> issueLabels = new ArrayList<>();

		Issue issue = Issue.of(repository, dto.getTitle(), false, dto.getState(), dto.getCreatedAt(),
			dto.getUpdatedAt(), dto.getClosedAt(), dto.getGhIssueId(), issueLabels);

		dto.getLabels().forEach(labelDto -> {
			Label label = labelService.findOrCreateLabel(labelDto.getName(), labelDto.getColor());
			IssueLabel issueLabel = IssueLabel.of(issue, label);
			issueLabels.add(issueLabel);
		});

		return issue;
	}

	private Set<IssueLabel> collectUniqueIssueLabels(List<Issue> issues) {
		return issues.stream().flatMap(issue -> issue.getIssueLabels().stream()).collect(Collectors.toSet());
	}

	public PagedRepositoryIssuesResponse getPagedRepositoryIssuesResponse(Repository repository, String sort,
		String order, String githubId, int page, int pageSize) {
		Sort.Direction direction = Sort.Direction.fromString(order);
		Sort sorting = Sort.by(direction, sort);
		Pageable pageable = PageRequest.of(page, pageSize, sorting);

		User user = userService.findGithubUser(githubId);
		Page<IssueWithPagedDto> issuePage = issueRepository.findIssuesWithPaged(repository.getId(), user.getId(),
			pageable);

		List<IssueResponse> issueResponseList = issuePage.getContent().stream().map(this::createIssueResponse).toList();

		return PagedRepositoryIssuesResponse.of(issuePage.getNumber(), issuePage.getSize(),
			issuePage.getTotalElements(), issuePage.getTotalPages(), repository.getName(), issueResponseList);
	}

	public StarRepositoryIssuesResponse getStaredRepositoryIssuesResponse(String githubId) {
		User user = userService.findGithubUser(githubId);
		List<IssueWithStarDto> issues = issueRepository.findTop5StarredIssuesForUserWithLabels(user.getId())
			.stream()
			.limit(ISSUE_STAR_SIZE)
			.toList();

		List<IssueStarResponse> issueResponseList = issues.stream().map(this::createIssueStarResponse).toList();

		return StarRepositoryIssuesResponse.of(issueResponseList);
	}

	@Transactional
	public void toggleIssueStar(String githubId, Long issueId) {
		User user = userService.findGithubUser(githubId);
		ErrorCode errorCode = ErrorCode.NOT_EXIST_ISSUE;
		Issue issue = issueRepository.findByGhIssueId(issueId)
			.orElseThrow(() -> new IssueNotFoundException(errorCode.getMessage(), errorCode.getStatus(),
				String.valueOf(issueId)));

		Optional<IssueStar> issueStar = issueStarRepository.findByUserAndIssue(user, issue);

		if (issueStar.isPresent()) {
			issueStarRepository.delete(issueStar.get());
			return;
		}
		issueStarRepository.save(IssueStar.of(user, issue));
	}

	public IssueDetailAndCommentsResponse getIssueDetailAndComments(String orgName, String repoName, String issueNumber, String githubId) {
		String accessToken = githubTokenService.findAccessToken(githubId);
		IssueDetailDto issueDetailDto = getGithubIssueDetail(orgName, repoName, issueNumber, accessToken);
		List<CommentsDto> commentsDtoList = getGithubIssueComments(orgName, repoName, issueNumber, accessToken);
		return IssueDetailAndCommentsResponse.of(issueDetailDto, commentsDtoList);
	}

	private IssueDetailDto getGithubIssueDetail(String orgName, String repoName, String issueNumber,
		String accessToken) {
		try {
			return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}/issues/{issue_number}")
					.build(orgName, repoName, issueNumber))
				.header("accept", "application/vnd.github+json")
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.bodyToMono(IssueDetailDto.class)
				.block();
		} catch (WebClientResponseException e) {
			throw new GithubApiException(e.getStatusCode(), e.getResponseBodyAsString());
		}
	}

	private List<CommentsDto> getGithubIssueComments(String orgName, String repoName, String issueNumber,
		String accessToken) {
		try {
			return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}/issues/{issue_number}/comments")
					.build(orgName, repoName, issueNumber))
				.header("accept", "application/vnd.github+json")
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.bodyToFlux(CommentsDto.class)
				.collectList()
				.block();
		} catch (WebClientResponseException e) {
			throw new GithubApiException(e.getStatusCode(), e.getResponseBodyAsString());
		}
	}

	private IssueResponse createIssueResponse(IssueWithPagedDto issueDto) {
		List<Label> labels = labelService.getLabelsByIssueId(issueDto.getIssue().getId());
		return IssueResponse.of(issueDto.getIssue().getId(), issueDto.getIssue().getGhIssueId(),
			issueDto.getIssue().getState(), issueDto.getIssue().getTitle(), labelService.convertLabelsResponse(labels),
			issueDto.getIssue().isRead(), issueDto.getIssue().getCreatedAt(), issueDto.getIssue().getUpdatedAt(),
			issueDto.getIssue().getClosedAt(), issueDto.isStarred());
	}

	private IssueStarResponse createIssueStarResponse(IssueWithStarDto issueDto) {
		List<Label> labels = labelService.getLabelsByIssueId(issueDto.getIssue().getId());
		return IssueStarResponse.of(issueDto.getIssue().getId(), issueDto.getOrgName(), issueDto.getRepositoryName(),
			issueDto.getIssue().getGhIssueId(), issueDto.getIssue().getState(), issueDto.getIssue().getTitle(),
			labelService.convertLabelsResponse(labels), issueDto.getIssue().isRead(),
			issueDto.getIssue().getCreatedAt(), issueDto.getIssue().getUpdatedAt(), issueDto.getIssue().getClosedAt(),
			issueDto.isStarred());
	}
}