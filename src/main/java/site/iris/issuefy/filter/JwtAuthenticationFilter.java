package site.iris.issuefy.filter;

import static org.springframework.http.HttpHeaders.*;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import site.iris.issuefy.exception.UnauthenticatedException;
import site.iris.issuefy.service.TokenProvider;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	public static final String BEARER_DELIMITER = "Bearer ";

	private final TokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		try {
			String token = getJwtFromRequest(request);

			if (!tokenProvider.isValidJwt(token)) {
				throw new UnauthenticatedException();
			}

			filterChain.doFilter(request, response);
		} catch (UnauthenticatedException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
			throw e;
		}
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION);

		if (bearerToken == null || !bearerToken.startsWith(BEARER_DELIMITER)) {
			throw new UnauthenticatedException();
		}

		return bearerToken.substring(BEARER_DELIMITER.length());
	}
}
