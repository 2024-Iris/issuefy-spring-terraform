package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.iris.issuefy.ApiDocumentUtils.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.model.dto.SubscriptionListDto;
import site.iris.issuefy.model.vo.RepositoryRecord;
import site.iris.issuefy.response.PagedSubscriptionResponse;
import site.iris.issuefy.service.GithubTokenService;
import site.iris.issuefy.service.SubscriptionService;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "issuefy.site", uriPort = -1)
class SubscriptionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SubscriptionService subscriptionService;

	@MockBean
	private GithubTokenService githubTokenService;

	@DisplayName("구독 중인 repository 목록을 조회한다.")
	@Test
	void getSubscribedRepositories() throws Exception {
		//given
		String token = "Bearer testToken";
		String githubId = "testGithubId";

		List<SubscriptionListDto> subscriptionResponses = List.of(
			new SubscriptionListDto(
				6764390L,
				"elastic",
				507775L,
				"elasticsearch",
				LocalDateTime.of(2024, 10, 23, 21, 26, 34),
				true
			)
		);

		PagedSubscriptionResponse response = PagedSubscriptionResponse.of(
			0, 15, 29, 2, subscriptionResponses
		);

		when(subscriptionService.getSubscribedRepositories(
			eq(githubId),
			eq(0),
			eq(15),
			eq("latestUpdateAt"),
			eq("desc"),
			eq(false)
		)).thenReturn(response);

		// when
		ResultActions result = mockMvc.perform(get("/api/subscriptions")
			.param("page", "0")
			.param("sort", "latestUpdateAt")
			.param("order", "desc")
			.param("starred", "false")
			.header("Authorization", token)
			.requestAttr("githubId", githubId));

		// then
		result.andExpect(status().isOk())
			.andDo(document("issuefy/subscriptions/get",
				getDocumentRequest(),
				getDocumentResponse(),
				queryParameters(
					parameterWithName("page").description("페이지 번호").optional(),
					parameterWithName("sort").description("정렬 기준").optional(),
					parameterWithName("order").description("정렬 순서 (asc/desc)").optional(),
					parameterWithName("starred").description("즐겨찾기 여부").optional()
				),
				responseFields(
					fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
						.description("현재 페이지 번호"),
					fieldWithPath("pageSize").type(JsonFieldType.NUMBER)
						.description("페이지 크기"),
					fieldWithPath("totalElements").type(JsonFieldType.NUMBER)
						.description("전체 항목 수"),
					fieldWithPath("totalPages").type(JsonFieldType.NUMBER)
						.description("전체 페이지 수"),
					fieldWithPath("subscriptionListDtos").type(JsonFieldType.ARRAY)
						.description("구독 리포지토리 목록"),
					fieldWithPath("subscriptionListDtos[].orgId").type(JsonFieldType.NUMBER)
						.description("조직 ID"),
					fieldWithPath("subscriptionListDtos[].orgName").type(JsonFieldType.STRING)
						.description("조직 이름"),
					fieldWithPath("subscriptionListDtos[].githubRepositoryId").type(JsonFieldType.NUMBER)
						.description("GitHub 리포지토리 ID"),
					fieldWithPath("subscriptionListDtos[].repositoryName").type(JsonFieldType.STRING)
						.description("리포지토리 이름"),
					fieldWithPath("subscriptionListDtos[].repositoryLatestUpdateAt").type(JsonFieldType.STRING)
						.description("리포지토리 최근 업데이트 시간"),
					fieldWithPath("subscriptionListDtos[].repositoryStarred").type(JsonFieldType.BOOLEAN)
						.description("즐겨찾기 여부")
				)
			));
	}

	@DisplayName("구독할 repository를 등록한다.")
	@Test
	void addRepository() throws Exception {
		// given
		String token = "Bearer testToken";
		String githubId = "testUser";
		String repositoryUrl = "https://github.com/2024-Iris/issuefy-spring";
		RepositoryRecord repositoryUrlVo = new RepositoryRecord(repositoryUrl);

		// when
		ResultActions result = mockMvc.perform(post("/api/subscriptions")
			.requestAttr("githubId", githubId)
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(repositoryUrlVo)));

		// then
		result.andExpect(status().isCreated())
			.andDo(document("issuefy/subscriptions/post",
				preprocessRequest(prettyPrint(),
					replacePattern(
						Pattern.compile("\"repositoryUrl\"\\s*:\\s*\"[^\"]*\""),
						"\"repositoryUrl\" : \"https://github.com/org/repository\""
					)),
				getDocumentResponse(),
				requestFields(
					fieldWithPath("repositoryUrl").type(JsonFieldType.STRING)
						.description("GitHub 리포지토리 URL")
				)
			));
	}

	@Test
	@DisplayName("구독하고 있는 리포지토리를 삭제한다.")
	void unsubscribeRepository() throws Exception {
		// given
		String token = "Bearer testToken";
		String githubId = "testUser";
		Long ghRepoId = 1L;

		doNothing().when(subscriptionService).unsubscribeRepository(ghRepoId);

		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/subscriptions/{gh_repo_id}", ghRepoId)
				.requestAttr("githubId", githubId)
				.header("Authorization", token))
			.andExpect(status().isNoContent())
			.andDo(document("issuefy/subscriptions/delete",
				getDocumentRequest(),
				getDocumentResponse(),
				pathParameters(
					parameterWithName("gh_repo_id").description("GitHub 리포지토리 ID")
				)
			));

		verify(subscriptionService).unsubscribeRepository(ghRepoId);
	}

	@Test
	@DisplayName("리포지토리 즐겨찾기를 토글한다.")
	void toggleStarRepository() throws Exception {
		// given
		String token = "Bearer testToken";
		String githubId = "testUser";
		Long ghRepoId = 1L;

		doNothing().when(subscriptionService).toggleRepositoryStar(githubId, ghRepoId);

		// when
		ResultActions result = mockMvc.perform(
			RestDocumentationRequestBuilders.put("/api/subscriptions/star/{gh_repo_id}", ghRepoId)
				.requestAttr("githubId", githubId)
				.header("Authorization", token));

		// then
		result.andExpect(status().isNoContent())
			.andDo(document("issuefy/subscriptions/star",
				getDocumentRequest(),
				getDocumentResponse(),
				pathParameters(
					parameterWithName("gh_repo_id").description("GitHub 리포지토리 ID")
				)
			));

		verify(subscriptionService).toggleRepositoryStar(githubId, ghRepoId);
	}
}