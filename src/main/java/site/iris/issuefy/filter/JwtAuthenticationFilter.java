package site.iris.issuefy.filter;

import static org.springframework.http.HttpHeaders.*;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.component.LambdaKey;
import site.iris.issuefy.exception.UnauthenticatedException;
import site.iris.issuefy.service.TokenProvider;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	public static final String BEARER_DELIMITER = "Bearer ";
	public static final int UUID_BEGIN_IDX = 0;
	public static final int UUID_END_IDX = 8;

	private final TokenProvider tokenProvider;
	private final LambdaKey lambdaKey;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String requestId = UUID.randomUUID().toString().substring(UUID_BEGIN_IDX, UUID_END_IDX);
		String clientIP = getClientIp(request);
		String path = request.getRequestURI();

		MDC.put("requestId", requestId);
		MDC.put("clientIP", clientIP);
		MDC.put("requestURL", path);

		try {
			// 프리플라이트 요청 처리
			if (request.getMethod().equals("OPTIONS")) {
				MDC.put("githubId", "OPTIONS");
				filterChain.doFilter(request, response);
				return;
			}

			// 인증이 필요없는 경로 처리
			if (path.startsWith("/api/login") || path.equals("/api/docs") || path.equals("/api/health")) {
				MDC.put("githubId", "Anonymous");
				filterChain.doFilter(request, response);
				return;
			}

			// Lambda 함수 요청 처리
			if (path.startsWith("/api/receive")) {
				String bearerToken = request.getHeader(AUTHORIZATION);
				if (bearerToken.equals(BEARER_DELIMITER + lambdaKey.getKey())) {
					MDC.put("githubId", "LambdaFunction");
					filterChain.doFilter(request, response);
					return;
				}
			}

			// JWT 토큰 처리
			String token = getJwtFromRequest(request);
			if (!tokenProvider.isValidToken(token)) {
				MDC.put("githubId", "JwtFilter");
				throw new UnauthenticatedException(UnauthenticatedException.ACCESS_TOKEN_EXPIRED,
					HttpStatus.FORBIDDEN.value());
			}

			Claims claims = tokenProvider.getClaims(token);
			String githubId = (String)claims.get("githubId");
			MDC.put("githubId", githubId);
			request.setAttribute("githubId", githubId);

			filterChain.doFilter(request, response);

		} catch (UnauthenticatedException e) {
			handleUnauthorizedException(response, e);
		} finally {
			MDC.clear();
		}
	}

	private String getClientIp(HttpServletRequest request) {
		String clientIP = request.getHeader("X-Forwarded-For");
		if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
			clientIP = request.getRemoteAddr();
		}
		return clientIP;
	}

	private void handleUnauthorizedException(HttpServletResponse response, UnauthenticatedException e)
		throws IOException {
		log.warn("Status: {}, Message: {}",
			e.getStatusCode(),
			e.getMessage());

		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
		response.setHeader("Access-Control-Allow-Headers", "*");
		response.setStatus(e.getStatusCode());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"message\":\"" + e.getMessage() + "\"}");
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken;

		try {
			bearerToken = request.getHeader(AUTHORIZATION);
			if (bearerToken == null || !bearerToken.startsWith(BEARER_DELIMITER)) {
				throw new UnauthenticatedException(UnauthenticatedException.INVALID_TOKEN_TYPE,
					HttpStatus.UNAUTHORIZED.value());
			}
		} catch (UnauthenticatedException e) {
			throw new UnauthenticatedException(UnauthenticatedException.INVALID_HEADER,
				HttpStatus.UNAUTHORIZED.value());
		}

		return bearerToken.substring(BEARER_DELIMITER.length());
	}
}
