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
	void connect() throws Exception {
		String githubId = "testUser";

		mockMvc.perform(get("/api/connect")
				.requestAttr("githubId", githubId)
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE))
			.andExpect(status().isOk())
			.andExpect(request().asyncStarted())
			.andDo(document("sse-connect"));
	}

	@Test
	void receive() throws Exception {
		UpdateRepositoryDto dto = new UpdateRepositoryDto(Arrays.asList("1", "2"));

		mockMvc.perform(post("/api/receive")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andDo(document("sse-receive",
				requestFields(
					fieldWithPath("updatedRepositoryIds").description("List of updated repository IDs")
				)
			));

		verify(notificationService).handleRedisMessage(any(UpdateRepositoryDto.class));
	}
}