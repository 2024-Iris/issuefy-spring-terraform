package site.iris.issuefy.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthCheckController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "issuefy.site", uriPort = -1)
class HealthCheckControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@DisplayName("서버가 현재 정상적인지 확인한다.")
	@Test
	void testHealthCheck() throws Exception {
		mockMvc.perform(get("/api/health"))
			.andExpect(status().isOk())
			.andDo(document("health-check",
				responseBody()));
	}
}