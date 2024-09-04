package site.iris.issuefy.filter;

import static org.springframework.http.HttpHeaders.*;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
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
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.exception.security.UnauthenticatedException;
import site.iris.issuefy.service.TokenProvider;
import site.iris.issuefy.util.ContainerIdUtil;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final String BEARER_DELIMITER = "Bearer ";
	private static final int UUID_BEGIN_IDX = 0;
	private static final int UUID_END_IDX = 8;
	private static final int VISIBLE_CHARS_FRONT = 2;
	private static final int VISIBLE_CHARS_BACK = 2;
	private static final int MIN_LENGTH_FOR_MASKING = 5;
	private static final char MASK_CHAR = '*';

	private final TokenProvider tokenProvider;
	private final LambdaKey lambdaKey;

	public static String maskId(String githubId) {
		if (githubId == null || githubId.length() < MIN_LENGTH_FOR_MASKING) {
			return githubId;
		}

		int totalVisibleChars = VISIBLE_CHARS_FRONT + VISIBLE_CHARS_BACK;
		int maskedLength = Math.max(0, githubId.length() - totalVisibleChars);
		String maskedPart = String.valueOf(MASK_CHAR).repeat(maskedLength);

		return githubId.substring(0, VISIBLE_CHARS_FRONT) + maskedPart + githubId.substring(
			githubId.length() - VISIBLE_CHARS_BACK);
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String requestId = UUID.randomUUID().toString().substring(UUID_BEGIN_IDX, UUID_END_IDX);
		String clientIP = getClientIp(request);
		String path = request.getRequestURI();

		MDC.put("containerId", ContainerIdUtil.containerId);
		MDC.put("requestId", requestId);
		MDC.put("requestURL", path);

		try {
			// 프리플라이트 요청 처리
			if (request.getMethod().equals("OPTIONS")) {
				MDC.put("user", "OPTIONS");
				filterChain.doFilter(request, response);
				return;
			}

			// 인증이 필요없는 경로 처리
			if (path.startsWith("/api/login") || path.equals("/api/docs") || path.equals("/api/health")) {
				MDC.put("user", "Anonymous");
				filterChain.doFilter(request, response);
				return;
			}

			// Lambda 함수 요청 처리
			if (path.startsWith("/api/receive")) {
				String bearerToken = request.getHeader(AUTHORIZATION);
				if (bearerToken.equals(BEARER_DELIMITER + lambdaKey.getKey())) {
					MDC.put("user", "LambdaFunction");
					filterChain.doFilter(request, response);
					return;
				}
			}

			// JWT 토큰 처리
			String token = getJwtFromRequest(request);

			if (!tokenProvider.isValidToken(token)) {
				MDC.put("user", "JwtFilter");
				throw new UnauthenticatedException(ErrorCode.ACCESS_TOKEN_EXPIRED.getMessage(),
					ErrorCode.ACCESS_TOKEN_EXPIRED.getStatus());
			}

			Claims claims = tokenProvider.getClaims(token);
			String githubId = (String)claims.get("githubId");

			//TODO 아이디 해시값으로 처리
			MDC.put("user", maskId(githubId));
			request.setAttribute("githubId", githubId);

			filterChain.doFilter(request, response);

		} catch (UnauthenticatedException e) {
			handleUnauthorizedException(clientIP, path, response, e);
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

	private void handleUnauthorizedException(String clientIP, String path, HttpServletResponse response,
		UnauthenticatedException e) throws IOException {
		log.warn("ClientIP: {}, RequestURL : {}, Message: {}", clientIP, path, e.getMessage());

		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
		response.setHeader("Access-Control-Allow-Headers", "*");
		response.setStatus(e.getStatus().value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"message\":\"" + e.getMessage() + "\"}");
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION);
		if (bearerToken == null) {
			throw new UnauthenticatedException(ErrorCode.INVALID_HEADER.getMessage(),
				ErrorCode.INVALID_HEADER.getStatus());
		}
		if (!bearerToken.startsWith(BEARER_DELIMITER)) {
			throw new UnauthenticatedException(ErrorCode.INVALID_TOKEN_TYPE.getMessage(),
				ErrorCode.INVALID_TOKEN_TYPE.getStatus());
		}
		return bearerToken.substring(BEARER_DELIMITER.length());
	}
}
