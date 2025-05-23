package site.iris.issuefy.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.Jwt;
import site.iris.issuefy.filter.JwtAuthenticationFilter;
import site.iris.issuefy.model.dto.UserDto;
import site.iris.issuefy.response.OauthResponse;
import site.iris.issuefy.service.AuthenticationService;
import site.iris.issuefy.service.TokenProvider;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthenticationController {
	private final AuthenticationService authenticationService;
	private final TokenProvider tokenProvider;

	@GetMapping("/login")
	public ResponseEntity<OauthResponse> login(@RequestParam String code) {
		UserDto userDto = authenticationService.githubLogin(code);
		Map<String, Object> claims = new HashMap<>();
		claims.put("githubId", userDto.getGithubId());
		Jwt jwt = tokenProvider.createJwt(claims);

		MDC.put("user", JwtAuthenticationFilter.maskId(userDto.getGithubId()));
		return ResponseEntity.ok()
			.body(OauthResponse.of(userDto.getGithubId(), userDto.getEmail(), userDto.getGithubProfileImage(),
				userDto.isAlertStatus(), jwt
			));
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
		String jwtToken = token.replace("Bearer ", "");
		tokenProvider.invalidateToken(jwtToken);
		return ResponseEntity.ok().build();
	}
}
