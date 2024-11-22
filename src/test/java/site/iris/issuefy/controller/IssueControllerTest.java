package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.iris.issuefy.ApiDocumentUtils.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import site.iris.issuefy.response.IssueResponse;
import site.iris.issuefy.response.LabelResponse;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;
import site.iris.issuefy.response.StarRepositoryIssuesResponse;
import site.iris.issuefy.service.IssueService;

@WebMvcTest(IssueController.class)
@AutoConfigureRestDocs
class IssueControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IssueService issueService;

	@DisplayName("해당 리포지토리에 오픈되어 있는 이슈를 조회한다.")
	@Test
	void getIssuesByRepoName() throws Exception {
		// given
		String orgName = "iris";
		String repoName = "issuefy";
		String githubId = "dokkisan";
		int page = 0;
		int pageSize = 15;
		String sort = "createdAt";
		String order = "desc";

		IssueResponse sampleIssue = new IssueResponse(
			1L,
			123L,
			"OPEN",
			"Sample Issue",
			List.of(new LabelResponse()),
			true,
			LocalDateTime.now(),
			LocalDateTime.now(),
			null,
			false
		);

		PagedRepositoryIssuesResponse response = new PagedRepositoryIssuesResponse(
			0, 15, 100, 10, repoName,
			List.of(sampleIssue)
		);

		// when
		when(issueService.getRepositoryIssues(orgName, repoName, githubId, page, pageSize, sort, order))
			.thenReturn(response);
		ResultActions result = mockMvc.perform(
			get("/api/subscriptions/{org_name}/{repo_name}/issues", orgName, repoName)
				.requestAttr("githubId", githubId)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(pageSize))
				.param("sort", sort)
				.param("order", order)
				.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk())
			.andDo(document("issuefy/issues/get",
				getDocumentRequest(),
				getDocumentResponse(),
				pathParameters(
					parameterWithName("org_name").description("조직 이름"),
					parameterWithName("repo_name").description("리포지토리 이름")
				),
				queryParameters(
					parameterWithName("page").description("페이지 번호").optional(),
					parameterWithName("size").description("페이지 크기").optional(),
					parameterWithName("sort").description("정렬 기준").optional(),
					parameterWithName("order").description("정렬 순서").optional()
				),
				responseFields(
					fieldWithPath("currentPage").description("현재 페이지 번호"),
					fieldWithPath("pageSize").description("페이지 크기"),
					fieldWithPath("totalElements").description("전체 이슈 수"),
					fieldWithPath("totalPages").description("전체 페이지 수"),
					fieldWithPath("repositoryName").description("리포지토리 이름"),
					fieldWithPath("issues").description("이슈 목록"),
					fieldWithPath("issues[].id").description("이슈 ID"),
					fieldWithPath("issues[].githubIssueNumber").description("GitHub 이슈 번호"),
					fieldWithPath("issues[].state").description("이슈 상태"),
					fieldWithPath("issues[].title").description("이슈 제목"),
					fieldWithPath("issues[].labels").description("라벨 목록"),
					fieldWithPath("issues[].labels[].name").description("라벨 이름"),
					fieldWithPath("issues[].labels[].color").description("라벨 색상"),
					fieldWithPath("issues[].read").description("읽음 여부"),
					fieldWithPath("issues[].createdAt").description("생성일시"),
					fieldWithPath("issues[].updatedAt").description("수정일시"),
					fieldWithPath("issues[].closedAt").description("종료일시"),
					fieldWithPath("issues[].starred").description("스타 여부")
				)
			));
	}

	@DisplayName("사용자가 스타를 준 이슈 목록을 조회한다.")
	@Test
	void getIssueStar() throws Exception {
		// given
		String githubId = "dokkisan";
		StarRepositoryIssuesResponse response = new StarRepositoryIssuesResponse(new ArrayList<>());

		// when
		when(issueService.getStarredRepositoryIssuesResponse(githubId)).thenReturn(response);
		ResultActions result = mockMvc.perform(
			get("/api/subscriptions/issue-star")
				.requestAttr("githubId", githubId));

		// then
		result.andExpect(status().isOk())
			.andDo(document("issuefy/issues/get-star",
				getDocumentRequest(),
				getDocumentResponse()
			));
	}

	@DisplayName("이슈의 스타 상태를 토글한다.")
	@Test
	void updateIssueStar() throws Exception {
		// given
		String githubId = "dokkisan";
		Long issueId = 123L;

		// when
		doNothing().when(issueService).toggleIssueStar(githubId, issueId);
		ResultActions result = mockMvc.perform(
			put("/api/subscriptions/issue-star/{gh_issue_id}", issueId)
				.requestAttr("githubId", githubId));

		// then
		result.andExpect(status().isNoContent())
			.andDo(document("issuefy/issues/update-star",
				getDocumentRequest(),
				getDocumentResponse(),
				pathParameters(
					parameterWithName("gh_issue_id").description("GitHub 이슈 ID")
				)
			));
	}
}