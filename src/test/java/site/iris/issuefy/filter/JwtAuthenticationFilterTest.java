package site.iris.issuefy.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.jsonwebtoken.Claims;
import site.iris.issuefy.entity.Jwt;

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
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.addHeader("Authorization", "Bearer validToken");

		Map<String, Object> claimsMap = new HashMap<>();
		claimsMap.put("githubId", "dokkisan");

		Claims claims = mock(Claims.class);
		when(claims.get("githubId")).thenReturn("dokkisan");

		Jwt jwt = mock(Jwt.class);
		when(jwt.getAccessToken()).thenReturn("validToken");

		when(tokenProvider.isValidToken("validToken")).thenReturn(true);
		when(tokenProvider.getClaims("validToken")).thenReturn(claims);

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(200, response.getStatus());
		assertEquals("dokkisan", request.getAttribute("githubId"));
	}

	@DisplayName("만료기간이 지난 토큰은 UnauthorizedException을 발생시킨다")
	@Test
	void testInvalidToken() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.addHeader("Authorization", "Bearer invalidToken");

		when(tokenProvider.isValidToken("invalidToken")).thenReturn(false);

		// when
		assertThrows(UnauthenticatedException.class, () -> {
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
		});

		// then
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
	}

	@DisplayName("Authorization 헤더가 없을 경우 UnauthorizedException을 발생시킨다.")
	@Test
	void testMissingAuthorizationHeader() {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// when
		assertThrows(UnauthenticatedException.class, () -> {
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
		});

		// then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}

	@DisplayName("Bearer 토큰이 아닐 경우 UnauthorizedException을 발생시킨다.")
	@Test
	void testInvalidTokenType() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.addHeader("Authorization", "invalidTokenType");

		// when
		assertThrows(UnauthenticatedException.class, () -> {
			jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
		});

		// then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}

	@DisplayName("API 명세서 url 접속시 필터링을 하지 않는다")
	@Test
	void doFilter_shouldPassThroughForDocsPath() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.setRequestURI("/api/docs");

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(200, response.getStatus());
	}

	@DisplayName("로그인 url 접속시 필터링을 하지 않는다")
	@Test
	void doFilter_shouldPassThroughForLoginPath() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.setRequestURI("/api/login");

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(200, response.getStatus());
	}
}