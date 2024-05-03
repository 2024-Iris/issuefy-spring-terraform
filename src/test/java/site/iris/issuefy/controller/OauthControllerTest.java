package site.iris.issuefy.controller;

import static org.hamcrest.Matchers.*;
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

import site.iris.issuefy.service.OauthService;
import site.iris.issuefy.vo.UserDto;

@WebMvcTest(OauthController.class)
@AutoConfigureRestDocs
class OauthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OauthService oauthService;

	@DisplayName("GitHub 로그인 후 사용자 정보와 JWT 반환한다.")
	@Test
	void login_after_return_userInfoAndJWT() throws Exception {

		// given
		String code = "test-auth-code";
		String userName = "testUser";
		String avatarURL = "https://avatars.githubusercontent.com/12345";
		String JWT = "RETURNTESTJWT";

		UserDto userDto = UserDto.of(userName, avatarURL);
		when(oauthService.githubLogin(code)).thenReturn(userDto);

		// when
		ResultActions result = mockMvc.perform(get("/api/login/{authorizationCode}", code)
			.param("code", code)
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.userName", is(userName)))
			.andExpect(jsonPath("$.avatarURL", is(avatarURL)))
			.andExpect(jsonPath("$.jwt", is(JWT)))
			.andDo(document("issuefy/oauth/login",
				responseFields(
					fieldWithPath("userName").description("사용자 로그인 이름"),
					fieldWithPath("avatarURL").description("사용자 아바타 URL"),
					fieldWithPath("jwt").description("JWT 토큰")
				)));
	}
}
