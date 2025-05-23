package site.iris.issuefy.controller;

import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.model.dto.NotificationReadDto;
import site.iris.issuefy.service.NotificationService;

@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(NotificationController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "issuefy.site", uriPort = -1)
class NotificationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private NotificationService notificationService;

	@Test
	@DisplayName("사용자의 알림 목록을 조회하여 반환한다.")
	void getNotifications() throws Exception {
		String token = "Bearer testToken";
		String githubId = "testUser";
		List<NotificationDto> notificationDtos = Arrays.asList(
			NotificationDto.of(1L, "testOrg1", "testRepo1", LocalDateTime.now(), false),
			NotificationDto.of(2L, "testOrg2", "testRepo2", LocalDateTime.now(), false)
		);

		given(notificationService.findNotifications(githubId)).willReturn(notificationDtos);

		mockMvc.perform(get("/api/notifications")
				.requestAttr("githubId", githubId)
				.header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andDo(document("issuefy/notifications/get",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("[].userNotificationId").description("알림 ID").type(NUMBER),
					fieldWithPath("[].orgName").description("조직 이름").type(STRING),
					fieldWithPath("[].repositoryName").description("저장소 이름").type(STRING),
					fieldWithPath("[].notificationCreatedAt").description("생성 시간").type(STRING),
					fieldWithPath("[].read").description("읽음 여부").type(BOOLEAN)
				)
			));
	}

	@Test
	@DisplayName("각 알림을 읽음으로 업데이트 한다.")
	void updateNotification() throws Exception {
		String token = "Bearer testToken";
		List<Long> ids = Arrays.asList(1L, 2L);
		NotificationReadDto notificationReadDto = new NotificationReadDto(ids);

		mockMvc.perform(patch("/api/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(notificationReadDto))
				.header("Authorization", token))
			.andExpect(status().isOk())
			.andDo(document("issuefy/notifications/update",
				requestFields(
					fieldWithPath("userNotificationIds").description("업데이트할 알림 ID 목록")
				)
			));

		verify(notificationService).updateUserNotificationsAsRead(notificationReadDto);
	}
}