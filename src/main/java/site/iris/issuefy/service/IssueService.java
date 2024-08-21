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
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.exception.github.GithubApiException;
import site.iris.issuefy.exception.resource.IssueNotFoundException;
import site.iris.issuefy.exception.resource.RepositoryNotFoundException;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.response.IssueResponse;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;

@Service
public class IssueService {
	private static final ErrorCode issueError = ErrorCode.NOT_EXIST_ISSUE;
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

	public PagedRepositoryIssuesResponse getIssues(String orgName, String repoName, String githubId, int page,
		int pageSize,
		String sort, String order, boolean starred) {
		Repository repository = findRepositoryByName(repoName);

		boolean syncNeeded = syncRepositoryIssues(repository, orgName, repoName, githubId);

		// if (syncNeeded) {
		// 	// 동기화가 필요한 경우 새로운 데이터로 응답
		// 	List<Issue> updatedIssues = issueRepository.findAllByRepositoryId(repository.getId());
		// 	return createRepositoryIssuesResponse(repository, updatedIssues, sort, order, starred, page);
		// } else {
		// 	// 동기화가 필요 없는 경우 기존 데이터로 응답
		// 	List<Issue> existingIssues = issueRepository.findAllByRepositoryId(repository.getId());
		// 	return createRepositoryIssuesResponse(repository, existingIssues, sort, order, starred, page);
		// }

		return createRepositoryIssuesResponse(repository, sort, order, starred, page, pageSize);
	}

	// 새로운 이슈인지 아닌지 판별
	public boolean syncRepositoryIssues(Repository repository, String orgName, String repoName, String githubId) {
		boolean issuesExist = issueRepository.existsById(repository.getId());

		if (!issuesExist) {
			// 이슈 없으면 등록
			addNewIssue(orgName, repoName, githubId, repository);
			return true;
		}

		// 이슈가 있으면 기존 이슈과 비교
		return checkForUpdatesAndSyncIssues(orgName, repoName, githubId, repository);
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

	private boolean checkForUpdatesAndSyncIssues(String orgName, String repoName, String githubId,
		Repository repository) {

		// 이전 이슈와 현재 이슈의 업데이트 시간을 비교하고 그게 같으면 깃허브 이슈 아이디까지 비교
		List<IssueDto> githubIssues = fetchOpenGoodFirstIssuesFromGithub(orgName, repoName, githubId);
		Issue mostRecentLocalIssue = findMostRecentLocalIssue(repository.getId());

		// 비교했더니 그래서 db 업데이트 해야 합니까?
		boolean needUpdateIssue = convertAndCompareIssue(githubIssues, mostRecentLocalIssue);

		// 예 업데이트 합시다.
		if (needUpdateIssue) {
			updateLocalIssuesWithGithubData(githubIssues, repository);
		}

		// 안해도 됨
		return needUpdateIssue;
	}

	private void addNewIssue(String orgName, String repoName, String githubId, Repository repository) {
		List<IssueDto> githubIssues = fetchOpenGoodFirstIssuesFromGithub(orgName, repoName, githubId);
		List<Label> allLabels = new ArrayList<>();
		List<IssueLabel> issueLabels = new ArrayList<>();

		List<Issue> issues = githubIssues.stream()
			.map(dto -> createIssueEntityFromDto(repository, dto, allLabels, issueLabels))
			.collect(Collectors.toList());

		saveIssuesToDatabase(issues, issueLabels, allLabels);
	}

	private Issue findMostRecentLocalIssue(Long repositoryId) {
		return issueRepository.findFirstByRepositoryIdOrderByUpdatedAtDesc(repositoryId)
			.orElseThrow(() -> new IssueNotFoundException(issueError.getMessage(), issueError.getStatus(),
				repositoryId.toString()));
	}

	private boolean convertAndCompareIssue(List<IssueDto> githubIssues, Issue mostRecentLocalIssue) {
		return githubIssues.stream()
			.findFirst()
			.map(firstDto -> shouldFetchIssues(firstDto, mostRecentLocalIssue))
			.orElse(false);
	}

	private boolean shouldFetchIssues(IssueDto latestGithubIssue, Issue latestDbIssue) {
		boolean isGithubIssueNewer = latestGithubIssue.getUpdatedAt().isAfter(latestDbIssue.getUpdatedAt());
		boolean isSameIssue = latestGithubIssue.getGhIssueId().equals(latestDbIssue.getGhIssueId());

		if (isGithubIssueNewer) {
			return !isSameIssue;
		}
		return true;
	}

	// TODO 벌크 업서트 방법을 도입할지 논의 필요 기존 이슈 삭제후 다시 인서트 방식으로 구현
	//  레이블 기능 재검토 필요하여 레이블 기능 배제함
	@Transactional
	public void updateLocalIssuesWithGithubData(List<IssueDto> githubIssues, Repository repository) {
		List<Issue> updatedIssues = githubIssues.stream()
			.map(dto -> createIssue(dto, repository))
			.collect(Collectors.toList());

		issueRepository.saveAll(updatedIssues);
	}

	private Issue createIssue(IssueDto dto, Repository repository) {
		List<IssueLabel> issueLabels = dto.getLabels().stream().map(labelDto -> {
			Label label = labelService.findOrCreateLabel(labelDto.getName(), labelDto.getColor());
			return IssueLabel.of(null, label);
		}).toList();

		Issue issue = Issue.of(repository, dto.getTitle(), false, dto.getState(), dto.getCreatedAt(),
			dto.getUpdatedAt(), dto.getClosedAt(), dto.getGhIssueId(), issueLabels);

		// IssueLabel에 Issue 설정
		issueLabels.forEach(issueLabel -> issueLabel.setIssue(issue));

		return issue;
	}

	// public RepositoryIssuesResponse getRepositoryIssuesResponse(String repoName) {
	// 	Repository repository = findRepositoryByName(repoName);
	// List<Issue> issues = issueRepository.findAllByRepository_Id(repository.getId());
	// return new RepositoryIssuesResponse(repository.getName(), convertIssuesToResponse(issues));
	// }

	// private List<IssueResponse> convertIssuesToResponse(List<Issue> issues) {
	// 	return issues.stream().map(this::convertIssueToResponse).collect(Collectors.toList());
	// }

	private PagedRepositoryIssuesResponse createRepositoryIssuesResponse(Repository repository, String sort,
		String order,
		boolean starred, int page, int pageSize) {
		// 정렬, 필터링, 페이징 로직 적용

		Sort.Direction direction = Sort.Direction.fromString(order);
		Sort sorting = Sort.by(direction, sort);
		Pageable pageable = PageRequest.of(page, pageSize, sorting);

		Page<Issue> issuePage = issueRepository.findAllByRepository_Id(repository.getId(), pageable).orElseThrow();

		List<IssueResponse> issueResponseList = issuePage.getContent().stream()
			.map(issue -> IssueResponse.of(
				issue.getId(),
				issue.getGhIssueId(),
				issue.getState(),
				issue.getTitle(),
				new ArrayList<>(),
				issue.isRead(),
				issue.getCreatedAt(),
				issue.getUpdatedAt(),
				issue.getClosedAt()
			))
			.toList();

		return PagedRepositoryIssuesResponse.of(
			issuePage.getNumber(),
			issuePage.getSize(),
			issuePage.getTotalElements(),
			issuePage.getTotalPages(),
			repository.getName(),
			issueResponseList);
	}

	// private IssueResponse convertIssueToResponse(Issue issue) {
	// 	Optional<List<Label>> optionalLabels = labelService.getLabelsByIssueId(issue.getId());
	// 	return IssueResponse.of(issue.getId(), issue.getGhIssueId(), issue.getState(), issue.getTitle(),
	// 		labelService.convertLabelsResponse(optionalLabels), issue.isRead(), issue.getCreatedAt(),
	// 		issue.getUpdatedAt(), issue.getClosedAt());
	// }

	private Repository findRepositoryByName(String repositoryName) {
		return repositoryRepository.findByName(repositoryName)
			.orElseThrow(() -> new RepositoryNotFoundException(ErrorCode.NOT_EXIST_REPOSITORY.getMessage(),
				ErrorCode.NOT_EXIST_REPOSITORY.getStatus(), repositoryName));
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

	// private boolean shouldFetchIssues(IssueDto latestGithubIssue, Issue latestDbIssue) {
	// 	boolean isGithubIssueNewer = latestGithubIssue.getUpdatedAt().isAfter(latestDbIssue.getUpdatedAt());
	// 	boolean isSameIssue = latestGithubIssue.getGhIssueId().equals(latestDbIssue.getGhIssueId());
	//
	// 	if (isGithubIssueNewer) {
	// 		return !isSameIssue;
	// 	}
	// 	return true;
	// }
	//
	// private Issue createIssuesByDto(Repository repository, IssueDto issueDto, List<Label> allLabels,
	// 	List<IssueLabel> issueLabels) {
	// 	Issue issue = Issue.of(repository, issueDto.getTitle(), issueDto.isRead(), issueDto.getState(),
	// 		issueDto.getCreatedAt(), issueDto.getUpdatedAt(), issueDto.getClosedAt(), issueDto.getGhIssueId(),
	// 		issueLabels);
	//
	// 	issueDto.getLabels().forEach(labelDto -> {
	// 		Label label = labelService.findOrCreateLabel(labelDto.getName(), labelDto.getColor());
	// 		allLabels.add(label);
	//
	// 		IssueLabel issueLabel = IssueLabel.of(issue, label);
	// 		issueLabels.add(issueLabel);
	// 	});
	//
	// 	return issue;
	// }
	//
	// private Repository findRepositoryByName(String repositoryName) {
	// 	// TODO: 리포지토리 이름 변경 시 update 로직 구현 필요
	// 	return repositoryRepository.findByName(repositoryName)
	// 		.orElseThrow(() -> new RepositoryNotFoundException(ErrorCode.NOT_EXIST_REPOSITORY.getMessage(),
	// 			ErrorCode.NOT_EXIST_REPOSITORY.getStatus(), repositoryName));
	// }
	//
	// private List<IssueResponse> convertToResponse(List<Issue> issues) {
	// 	return issues.stream().map(issue -> {
	// 		Optional<List<Label>> optionalLabels = labelService.getLabelsByIssueId(issue.getId());
	//
	// 		return IssueResponse.of(issue.getId(), issue.getGhIssueId(), issue.getState(), issue.getTitle(),
	// 			labelService.convertLabelsResponse(optionalLabels), issue.isRead(), issue.getCreatedAt(),
	// 			issue.getUpdatedAt(), issue.getClosedAt());
	// 	}).toList();
	// }
	//
	// private Optional<List<IssueDto>> githubGetOpenGoodFirstIssues(String orgName, String repoName, String githubId) {
	// 	String accessToken = githubTokenService.findAccessToken(githubId);
	// 	try {
	// 		// TODO 일단 가져온 이슈 리스트의 최상단 이슈를
	// 		//  현재 db 이슈와 비교해서 다르면 업데이트 하고 그게아니면 저장할 필요가 없음
	// 		//  근데 또 이전에 저장한 이슈를 한번에 다 날리면 안됨 읽음/안읽음이 있음
	// 		return Optional.ofNullable(webClient.get()
	// 			.uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}/issues")
	// 				.queryParam("state", "open")
	// 				.queryParam("sort", "updated")
	// 				.queryParam("direction", "desc")
	// 				.queryParam("labels", "good first issue")
	// 				.build(orgName, repoName))
	// 			.header("accept", "application/vnd.github+json")
	// 			.header("Authorization", "Bearer " + accessToken)
	// 			.retrieve()
	// 			.bodyToFlux(IssueDto.class)
	// 			.collectList()
	// 			.block());
	// 	} catch (WebClientResponseException e) {
	// 		throw new GithubApiException(e.getStatusCode(), e.getResponseBodyAsString());
	// 	}
	// }
	//
	// private void saveAllEntities(List<Issue> issues, List<IssueLabel> issueLabels, List<Label> allLabels) {
	// 	issueRepository.saveAll(issues);
	// 	labelService.saveAllLabels(allLabels);
	// 	issueLabelRepository.saveAll(issueLabels);
	// }
	//
	// public RepositoryIssuesResponse getIssues(String orgName, String repoName, String githubId, int page, String sort,
	// 	String order, boolean starred) {
	//
	// 	Repository repository = findRepositoryByName(repoName);
	//
	// 	Pageable pageable = Pageable.ofSize(page);
	//
	// 	Optional<Page<Issue>> pagedIssues = issueRepository.findAllByRepository_Id(repository.getId(), pageable);
	//
	// 	pagedIssues.
	//
	// 	return
	// }
}
