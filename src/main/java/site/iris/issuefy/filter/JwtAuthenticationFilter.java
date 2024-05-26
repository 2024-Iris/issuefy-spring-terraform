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
import site.iris.issuefy.exception.UnauthenticatedException;
import site.iris.issuefy.service.TokenProvider;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	public static final String BEARER_DELIMITER = "Bearer ";

	private final TokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain)
		throws ServletException, IOException {

		String path = request.getRequestURI();
		if (path.startsWith("/api/login") || path.equals("/api/docs")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String token = getJwtFromRequest(request);

			// 토큰이 만료된 경우 403 반환
			if (!tokenProvider.isValidToken(token)) {
				throw new UnauthenticatedException(UnauthenticatedException.ACCESS_TOKEN_EXPIRED,
					HttpStatus.FORBIDDEN.value());
			}

			request.setAttribute("githubId", tokenProvider.getClaims(token).get("githubId"));

			filterChain.doFilter(request, response);
		} catch (UnauthenticatedException e) {
			logger.warn(e.getMessage());
			response.sendError(e.getStatusCode(), e.getMessage());
			throw e;
		}
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
