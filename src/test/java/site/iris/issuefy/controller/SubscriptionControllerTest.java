package site.iris.issuefy.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.iris.issuefy.ApiDocumentUtils.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.dto.SubscribedResponse;
import site.iris.issuefy.service.SubscriptionService;
import site.iris.issuefy.vo.RepoVO;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureRestDocs
class SubscriptionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SubscriptionService subscriptionService;

	@DisplayName("구독 중인 repository 목록을 조회한다.")
	@Test
	void getSubscribedRepositories() throws Exception {

		// given
		RepoVO repoVO = new RepoVO("issuefy", "iris");

		// when
		subscriptionService.getSubscribedRepositories();

		// then
		mockMvc.perform(get("/subscriptions"))
			.andExpect(status().isOk())
			.andDo(document("issuefy/subscriptions/get",
				getDocumentRequest(),
				getDocumentResponse()
			));
	}

	@DisplayName("구독할 repository를 등록한다.")
	@Test
	void create() throws Exception {

		// given
		RepoVO repoVO = new RepoVO("issuefy", "org");
		SubscribedResponse subscribedResponse = new SubscribedResponse();
		subscribedResponse.setId(1L);
		subscribedResponse.setName(repoVO.name());
		subscribedResponse.setOrg(repoVO.org());

		// when & then
		mockMvc.perform(post("/subscriptions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(repoVO)))
			.andExpect(status().isCreated())
			.andDo(document("issuefy/subscriptions/post",
				getDocumentRequest(),
				getDocumentResponse(),
				requestFields(
					fieldWithPath("name").type(JsonFieldType.STRING).description("리포지토리 이름"),
					fieldWithPath("org").type(JsonFieldType.STRING).description("조직 이름")
				),
				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("아이디"),
					fieldWithPath("name").type(JsonFieldType.STRING).description("리포지토리 이름"),
					fieldWithPath("org").type(JsonFieldType.STRING).description("조직 이름")
				)
			));
	}
}