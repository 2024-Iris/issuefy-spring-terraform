package site.iris.issuefy.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import site.iris.issuefy.exception.UnauthenticatedException;
import site.iris.issuefy.service.TokenProvider;

class JwtAuthenticationFilterTest {
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	private TokenProvider tokenProvider;

	@BeforeEach
	void setUp() {
		tokenProvider = mock(TokenProvider.class);
		jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenProvider);
	}

	@DisplayName("유효한 토큰은 필터를 통과한다")
	@Test
	void testValidToken() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer validToken");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		when(tokenProvider.isValidJwt("validToken")).thenReturn(true);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertEquals(200, response.getStatus());
	}

	@DisplayName("유효하지 않은 토큰은 UnauthorizedException을 발생시킨다")
	@Test
	void testInvalidToken() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer invalidToken");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		when(tokenProvider.isValidJwt("invalidToken")).thenReturn(false);

		assertThrows(UnauthenticatedException.class, () -> {
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
		});

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}

	@DisplayName("Authorization 헤더가 없을 경우 UnauthorizedException을 발생시킨다.")
	@Test
	void testMissingAuthorizationHeader() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		assertThrows(UnauthenticatedException.class, () -> {
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
		});

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}

	@DisplayName("Bearer 토큰이 아닐 경우 UnauthorizedException을 발생시킨다.")
	@Test
	void testInvalidTokenType() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "invalidTokenType");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		assertThrows(UnauthenticatedException.class, () -> {
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
		});

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}
}