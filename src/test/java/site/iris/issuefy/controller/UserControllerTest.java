package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import site.iris.issuefy.model.dto.UserDto;
import site.iris.issuefy.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureRestDocs
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@DisplayName("사용자 정보를 가져온다.")
	@Test
	void getUserInfo() throws Exception {
		// given
		String githubId = "testUser";
		UserDto userDto = UserDto.of(githubId, "https://example.com/avatar.jpg", "test@gmail.com", false);
		when(userService.getUserInfo(githubId)).thenReturn(userDto);

		// when
		ResultActions result = mockMvc.perform(get("/api/user")
			.requestAttr("githubId", githubId)
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.login").value(githubId))
			.andExpect(jsonPath("$.avatar_url").value("https://example.com/avatar.jpg"))
			.andExpect(jsonPath("$.email").value("test@gmail.com"))
			.andExpect(jsonPath("$.alertStatus").value(false))
			.andDo(document("get-user-info",
				responseFields(
					fieldWithPath("login").description("사용자의 GitHub ID"),
					fieldWithPath("avatar_url").description("사용자의 GitHub 프로필 이미지 URL"),
					fieldWithPath("email").description("사용자의 이메일 주소"),
					fieldWithPath("alertStatus").description("알림 상태")
				)));

		verify(userService).getUserInfo(githubId);
	}

	@DisplayName("사용자 이메일을 업데이트한다.")
	@Test
	void updateEmail() throws Exception {
		// given
		String githubId = "testUser";
		String newEmail = "newemail@gmail.com";
		String requestBody = "{\"email\":\"" + newEmail + "\"}";

		// when
		ResultActions result = mockMvc.perform(patch("/api/user/email")
			.requestAttr("githubId", githubId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody));

		// then
		result.andExpect(status().isNoContent())
			.andDo(document("update-email",
				requestFields(
					fieldWithPath("email").description("새로운 이메일 주소")
				)));

		verify(userService).updateEmail(githubId, newEmail);
	}

	@DisplayName("사용자 알림 상태를 업데이트한다.")
	@Test
	void updateAlert() throws Exception {
		// given
		String githubId = "testUser";
		boolean newAlertStatus = true;
		String requestBody = "{\"alertStatus\":" + newAlertStatus + "}";

		// when
		ResultActions result = mockMvc.perform(patch("/api/user/alert")
			.requestAttr("githubId", githubId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody));

		// then
		result.andExpect(status().isNoContent())
			.andDo(document("update-alert",
				requestFields(
					fieldWithPath("alertStatus").description("알림 받기 / 안받기")
				)));

		verify(userService).updateAlert(githubId, newAlertStatus);
	}
}