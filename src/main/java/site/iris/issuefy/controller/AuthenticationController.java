package site.iris.issuefy.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Jwt;
import site.iris.issuefy.model.dto.UserDto;
import site.iris.issuefy.response.OauthResponse;
import site.iris.issuefy.service.AuthenticationService;
import site.iris.issuefy.service.TokenProvider;
import site.iris.issuefy.service.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthenticationController {
	private final AuthenticationService authenticationService;
	private final TokenProvider tokenProvider;
	private final UserService userService;

	@GetMapping("/login")
	public ResponseEntity<OauthResponse> login(@RequestParam String code) {
		log.info("Login request occurs");
		UserDto userDto = authenticationService.githubLogin(code);
		Map<String, Object> claims = new HashMap<>();
		claims.put("githubId", userDto.getGithubId());
		Jwt jwt = tokenProvider.createJwt(claims);

		log.info("user login : {}, authorization code : {}", userDto.getGithubId(), code);
		log.info("response : {}", jwt.toString());
		return ResponseEntity.ok()
			.body(OauthResponse.of(userDto.getGithubId(), userDto.getEmail(), userDto.getGithubProfileImage(),
				userDto.isAlertStatus(), jwt
			));
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestAttribute String githubId,
		@RequestHeader("Authorization") String token) {
		log.info("user logout : {}", githubId);
		String jwtToken = token.replace("Bearer ", "");
		tokenProvider.invalidateToken(jwtToken);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/withdraw")
	public ResponseEntity<String> withdraw(@RequestAttribute String githubId) {
		log.info("user withdraw : {}", githubId);
		userService.withdraw(githubId);
		return ResponseEntity.ok().build();
	}
}
