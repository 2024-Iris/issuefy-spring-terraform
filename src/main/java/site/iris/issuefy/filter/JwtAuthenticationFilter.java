package site.iris.issuefy.filter;

import static org.springframework.http.HttpHeaders.*;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.exception.UnauthenticatedException;
import site.iris.issuefy.service.TokenProvider;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	public static final String BEARER_DELIMITER = "Bearer ";

	private final TokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain)
		throws ServletException, IOException {

		// 프리플라이트 요청 처리
		if (request.getMethod().equals("OPTIONS")) {
			handlePreflightRequest(response);
			return;
		}

		String path = request.getRequestURI();
		if (path.startsWith("/api/login") || path.equals("/api/docs")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String token = getJwtFromRequest(request);

			if (!tokenProvider.isValidToken(token)) {
				throw new UnauthenticatedException(UnauthenticatedException.ACCESS_TOKEN_EXPIRED,
					HttpStatus.FORBIDDEN.value());
			}

			request.setAttribute("githubId", tokenProvider.getClaims(token).get("githubId"));

			filterChain.doFilter(request, response);
		} catch (UnauthenticatedException e) {
			log.info(e.getMessage());
			handleUnauthorizedException(response, e);
		}
	}

	private void handlePreflightRequest(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
		response.setHeader("Access-Control-Allow-Headers", "*");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	private void handleUnauthorizedException(HttpServletResponse response, UnauthenticatedException e)
		throws IOException {
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
