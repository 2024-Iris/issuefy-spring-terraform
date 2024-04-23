package site.iris.issuefy.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static site.iris.issuefy.ApiDocumentUtils.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import site.iris.issuefy.service.SubscriptionService;
import site.iris.issuefy.vo.RepoVO;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "docs.api.com")
class SubscriptionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SubscriptionService subscriptionService;

	@DisplayName("구독 중인 repository 목록을 조회한다.")
	@Test
	void getSubscribedRepositories() throws Exception {

		// given
		RepoVO repoVO = new RepoVO("iris", "issuefy");

		// when
		subscriptionService.getSubscribedRepositories();

		// then
		mockMvc.perform(get("/subscriptions"))
			.andExpect(status().isOk())
			.andDo(document("issuefy/subscriptions",
				getDocumentRequest(),
				getDocumentResponse()
			));
	}
}