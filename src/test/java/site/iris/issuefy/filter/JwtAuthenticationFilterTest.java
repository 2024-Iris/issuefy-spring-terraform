package site.iris.issuefy.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import site.iris.issuefy.component.LambdaKey;
import site.iris.issuefy.entity.Jwt;
import site.iris.issuefy.exception.UnauthenticatedException;
import site.iris.issuefy.service.TokenProvider;

@EnabledIfEnvironmentVariable(named = "sonarCloud", matches = "true")
class JwtAuthenticationFilterTest {
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	private TokenProvider tokenProvider;
	private LambdaKey lambdaKey;

	@BeforeEach
	void setUp() {
		tokenProvider = mock(TokenProvider.class);
		jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenProvider, lambdaKey);
	}

	@DisplayName("유효한 토큰은 필터를 통과한다")
	@Test
	void testValidToken() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.addHeader("Authorization", "Bearer validToken");

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
	void testInvalidToken() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.addHeader("Authorization", "Bearer invalidToken");

		when(tokenProvider.isValidToken("invalidToken")).thenReturn(false);

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
		assertTrue(response.getContentAsString()
			.contains("\"message\":\"" + UnauthenticatedException.ACCESS_TOKEN_EXPIRED + "\""));
	}

	@DisplayName("Authorization 헤더가 없을 경우 UnauthorizedException을 발생시킨다.")
	@Test
	void testMissingAuthorizationHeader() throws ServletException, IOException {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
		assertTrue(
			response.getContentAsString().contains("\"message\":\"" + UnauthenticatedException.INVALID_HEADER + "\""));
	}

	@DisplayName("Bearer 토큰이 아닐 경우 UnauthorizedException을 발생시킨다.")
	@Test
	void testInvalidTokenType() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.addHeader("Authorization", "invalidTokenType");

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
		assertEquals("{\"message\":\"" + UnauthenticatedException.INVALID_HEADER + "\"}",
			response.getContentAsString());
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

	@DisplayName("프리플라이트 요청 처리")
	@Test
	void testPreflightRequest() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// given
		request.setMethod("OPTIONS");

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
}