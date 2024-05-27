package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.model.vo.RepositoryRecord;
import site.iris.issuefy.response.SubscribeResponse;
import site.iris.issuefy.service.GithubTokenService;
import site.iris.issuefy.service.SubscribeService;

@WebMvcTest(SubscribeController.class)
@AutoConfigureRestDocs
class SubscribeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SubscribeService subscribeService;

	@MockBean
	private GithubTokenService githubTokenService;

	@DisplayName("구독 중인 repository 목록을 조회한다.")
	@Test
	void getSubscribedRepositories() throws Exception {
		// given
		String token = "Bearer testToken";
		String githubId = "testGithubId";
		List<SubscribeResponse> subscribeResponses = new ArrayList<>();
		when(subscribeService.getSubscribedRepositories("testToken")).thenReturn(subscribeResponses);

		// when
		ResultActions result = mockMvc.perform(get("/api/subscribe")
			.header("Authorization", token)
			.requestAttr("githubId", githubId));

		// then
		result.andExpect(status().isOk())
			.andDo(document("issuefy/subscribe/get",
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
		ResultActions result = mockMvc.perform(post("/api/subscribe")
			.requestAttr("githubId", githubId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(repositoryUrlVo)));

		// then
		result.andExpect(status().isCreated())
			.andDo(document("issuefy/subscribe/post",
				getDocumentRequest(),
				getDocumentResponse(),
				requestFields(
					fieldWithPath("repositoryUrl").type(JsonFieldType.STRING).description("리포지토리 URL")
				)
			));
	}
}