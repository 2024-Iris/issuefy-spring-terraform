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
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.dto.RepositoryResponse;
import site.iris.issuefy.service.RepositoryService;
import site.iris.issuefy.vo.RepoVO;

@WebMvcTest(RepositoryController.class)
@AutoConfigureRestDocs
class RepositoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private RepositoryService repositoryService;

	@DisplayName("구독 중인 repository 목록을 조회한다.")
	@Test
	void getSubscribedRepositories() throws Exception {

		// given
		RepoVO repoVO = new RepoVO("issuefy", "iris");

		// when
		repositoryService.getSubscribedRepositories();

		// then
		mockMvc.perform(get("/repo"))
			.andExpect(status().isOk())
			.andDo(document("issuefy/repo/get",
				getDocumentRequest(),
				getDocumentResponse()
			));
	}

	@DisplayName("구독할 repository를 등록한다.")
	@Test
	void create() throws Exception {

		// given
		RepoVO repoVO = new RepoVO("issuefy", "org");
		RepositoryResponse repositoryResponse = RepositoryResponse.from(repoVO);

		// when
		ResultActions result = mockMvc.perform(post("/repo")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(repoVO)));

		// then
		result.andExpect(status().isCreated())
			.andDo(document("issuefy/repo/post",
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