package site.iris.issuefy.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import site.iris.issuefy.entity.Jwt;
import site.iris.issuefy.service.AuthenticationService;
import site.iris.issuefy.service.TokenProvider;
import site.iris.issuefy.model.dto.UserDto;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureRestDocs
class AuthenticationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthenticationService authenticationService;

	@MockBean
	private TokenProvider tokenProvider;

	@DisplayName("GitHub 엑세스 토큰을 가져오고 사용자 정보와 JWT를 반환한다.")
	@Test
	void login_after_return_userInfoAndJWT() throws Exception {

		// given
		String code = "test-auth-code";
		String userName = "testUser";
		String avatarURL = "https://avatars.githubusercontent.com/12345";
		String email = "test@gmail.com";

		UserDto userDto = UserDto.of(userName, avatarURL, email);
		when(authenticationService.githubLogin(code)).thenReturn(userDto);

		Map<String, Object> claims = new HashMap<>();
		claims.put("githubId", userDto.getGithubId());
		String accessToken = "test-jwt-token";
		String refreshToken = "test-refresh-token";
		Jwt jwt = new Jwt(accessToken, refreshToken);
        when(tokenProvider.createJwt(claims)).thenReturn(jwt);

		// when
		ResultActions result = mockMvc.perform(get("/api/login", code)
			.param("code", code)
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.userName", is(userName)))
			.andExpect(jsonPath("$.avatarURL", is(avatarURL)))
			.andExpect(jsonPath("$.jwt.accessToken", is(accessToken)))
            .andExpect(jsonPath("$.jwt.refreshToken", is(refreshToken)))
			.andDo(document("issuefy/oauth/login",
				responseFields(
					fieldWithPath("userName").description("사용자 로그인 이름"),
					fieldWithPath("avatarURL").description("사용자 아바타 URL"),
					fieldWithPath("jwt.accessToken").description("JWT 액세스 토큰"),
                    fieldWithPath("jwt.refreshToken").description("JWT 리프레시 토큰")
				)));
	}
}
