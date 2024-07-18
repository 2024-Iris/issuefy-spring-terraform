package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.iris.issuefy.ApiDocumentUtils.*;

import java.util.ArrayList;
import java.util.List;

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

import site.iris.issuefy.model.vo.RepositoryRecord;
import site.iris.issuefy.response.SubscriptionResponse;
import site.iris.issuefy.service.GithubTokenService;
import site.iris.issuefy.service.SubscriptionService;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureRestDocs
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
		// given
		String token = "Bearer testToken";
		String githubId = "testGithubId";
		List<SubscriptionResponse> subscriptionResponses = new ArrayList<>();
		when(subscriptionService.getSubscribedRepositories("testToken")).thenReturn(subscriptionResponses);

		// when
		ResultActions result = mockMvc.perform(get("/api/subscription")
			.header("Authorization", token)
			.requestAttr("githubId", githubId));

		// then
		result.andExpect(status().isOk())
			.andDo(document("issuefy/subscription/get",
				getDocumentRequest(),
				getDocumentResponse()
			));
	}

	@DisplayName("구독할 repository를 등록한다.")
	@Test
	void addRepository() throws Exception {
		// given
		String githubId = "testUser";
		String repositoryUrl = "https://github.com/2024-Iris/issuefy-spring";
		RepositoryRecord repositoryUrlVo = new RepositoryRecord(repositoryUrl);

		// when
		ResultActions result = mockMvc.perform(post("/api/subscription")
			.requestAttr("githubId", githubId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(repositoryUrlVo)));

		// then
		result.andExpect(status().isCreated())
			.andDo(document("issuefy/subscription/post",
				getDocumentRequest(),
				getDocumentResponse(),
				requestFields(
					fieldWithPath("repositoryUrl").type(JsonFieldType.STRING).description("리포지토리 URL")
				)
			));
	}

	@Test
	@DisplayName("구독하고 있는 리포지토리를 삭제한다.")
	void unsubscribeRepository() throws Exception {
		// given
		String githubId = "testUser";
		Long ghRepoId = 1L;

		doNothing().when(subscriptionService).unsubscribeRepository(ghRepoId);

		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/subscription/{gh_repo_id}", ghRepoId)
				.requestAttr("githubId", githubId))
			.andExpect(status().isNoContent())
			.andDo(document("unsubscribe-repository",
				pathParameters(
					parameterWithName("gh_repo_id").description("GitHub 저장소 ID")
				)
			));

		verify(subscriptionService).unsubscribeRepository(ghRepoId);
	}
}