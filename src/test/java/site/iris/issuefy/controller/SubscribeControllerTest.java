package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.iris.issuefy.ApiDocumentUtils.*;

import java.util.ArrayList;

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

import site.iris.issuefy.dto.SubscribeResponse;
import site.iris.issuefy.service.SubscribeService;
import site.iris.issuefy.vo.OrgRecord;
import site.iris.issuefy.vo.RepositoryVO;

@WebMvcTest(SubscribeController.class)
@AutoConfigureRestDocs
class SubscribeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SubscribeService subscribeService;

	@DisplayName("구독 중인 repository 목록을 조회한다.")
	@Test
	void getSubscribedRepositories() throws Exception {
		// given
		String token = "Bearer testToken";
		// when
		ResultActions result = mockMvc.perform(get("/api/subscribe")
			.header("Authorization", token));

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
		RepositoryVO repositoryVO = new RepositoryVO("issuefy", "iris");
		SubscribeResponse request = SubscribeResponse.from(OrgRecord.from(1L, "test", new ArrayList<>()));
		when(subscribeService.getSubscribedRepositories("asd")).thenReturn(new ArrayList<>());

		// when
		ResultActions result = mockMvc.perform(post("/api/subscribe")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)));

		// then
		result.andExpect(status().isCreated())
			.andDo(document("issuefy/subscribe/post",
				getDocumentRequest(),
				getDocumentResponse(),
				requestFields(
					fieldWithPath("name").type(JsonFieldType.STRING).description("리포지토리 이름"),
					fieldWithPath("org").type(JsonFieldType.STRING).description("조직 이름")
				),
				responseFields(
					fieldWithPath("org.id").type(JsonFieldType.NUMBER).description("조직 ID"),
					fieldWithPath("org.name").type(JsonFieldType.STRING).description("조직 이름"),
					fieldWithPath("org.repositories").type(JsonFieldType.ARRAY).description("조직의 리포지토리 목록")
				)
			));
	}
}
