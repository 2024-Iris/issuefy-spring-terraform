package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.iris.issuefy.ApiDocumentUtils.*;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import site.iris.issuefy.response.RepositoryIssuesResponse;
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
		RepositoryIssuesResponse response = new RepositoryIssuesResponse(repoName, new ArrayList<>());

		// when
		when(issueService.getRepositoryIssuesResponse(orgName, repoName, githubId))
			.thenReturn(response);
		ResultActions result = mockMvc.perform(
			get("/api/subscriptions/{org_name}/{repo_name}/issues", orgName, repoName)
				.requestAttr("githubId", githubId));

		// then
		result.andExpect(status().isOk())
			.andDo(document("issuefy/issues/get",
				getDocumentRequest(),
				getDocumentResponse(),
				pathParameters(
					parameterWithName("org_name").description("조직 이름"),
					parameterWithName("repo_name").description("리포지토리 이름"))
			));
	}
}