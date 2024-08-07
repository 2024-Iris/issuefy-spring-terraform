package site.iris.issuefy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.model.dto.UpdateRepositoryDto;
import site.iris.issuefy.service.NotificationService;

@WebMvcTest(SseController.class)
@AutoConfigureRestDocs
class SseControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private NotificationService notificationService;

	@Test
	@DisplayName("SSE 커넥션을 생성하고 연결한다.")
	void connect() throws Exception {
		String githubId = "testUser";

		mockMvc.perform(get("/api/connect")
				.requestAttr("githubId", githubId)
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE))
			.andExpect(status().isOk())
			.andExpect(request().asyncStarted())
			.andDo(document("issuefy/sse/connect"));
	}

	@Test
	@DisplayName("업데이트 된 리포지토리를 구독하는 유저에게 알림을 보낸다.")
	void receive() throws Exception {
		UpdateRepositoryDto dto = new UpdateRepositoryDto(Arrays.asList("1", "2"));

		mockMvc.perform(post("/api/receive")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andDo(document("issuefy/sse/receive",
				requestFields(
					fieldWithPath("updatedRepositoryIds").description("업데이트 된 리포지토리 ID 목록")
				)
			));

		verify(notificationService).handleRedisMessage(any(UpdateRepositoryDto.class));
	}
}