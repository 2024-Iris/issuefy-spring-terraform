package site.iris.issuefy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import site.iris.issuefy.response.DashBoardResponse;
import site.iris.issuefy.service.DashBoardService;

@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(DashBoardController.class)
@AutoConfigureRestDocs
class DashBoardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DashBoardService dashBoardService;

	@BeforeEach
	public void setUp(WebApplicationContext webApplicationContext,
		RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(documentationConfiguration(restDocumentation))
			.build();
	}

	@Test
	@DisplayName("대시보드에 필요한 랭크, 주간방문수, 리포지토리 추가 횟수를 반환한다.")
	void dashboard() throws Exception {
		DashBoardResponse mockResponse = DashBoardResponse.of("A", "10", "5");

		when(dashBoardService.getDashBoardFromLoki(anyString())).thenReturn(mockResponse);

		this.mockMvc.perform(get("/api/dashboard")
				.requestAttr("githubId", "testUser"))
			.andExpect(status().isOk())
			.andDo(document("dashboard",
				responseFields(
					fieldWithPath("rank").description("User's rank based on activity"),
					fieldWithPath("visitCount").description("Number of visits in the last week"),
					fieldWithPath("addRepositoryCount").description("Number of repositories added in the last week")
				)));
	}
}