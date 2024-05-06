package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import site.iris.issuefy.vo.UserDto;

class AuthenticationServiceTest {

	@Test
	@DisplayName("authenticationCode로 로그인 유저 정보를 반환한다.")
	void githubLogin() {
		// given
		String authenticationCode = "testCode";
		UserDto userDto = UserDto.of("testUser", "testUser");

		AuthenticationService authenticationService = mock(AuthenticationService.class);
		GithubAccessTokenService githubAccessTokenService = mock(GithubAccessTokenService.class);
		when(githubAccessTokenService.getToken(authenticationCode)).thenReturn(
			"access_token=testToken&scope=&token_type=bearer");
		when(authenticationService.githubLogin(authenticationCode)).thenReturn(userDto);

		// when
		UserDto result = authenticationService.githubLogin(authenticationCode);

		// then
		assertNotNull(result);
		assertEquals(userDto, result);
	}
}
