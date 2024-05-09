package site.iris.issuefy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.dto.OauthResponse;
import site.iris.issuefy.service.AuthenticationService;
import site.iris.issuefy.vo.UserDto;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
	private final AuthenticationService authenticationService;

	@GetMapping("/api/login")
	public ResponseEntity<OauthResponse> login(@RequestParam String code) {
		UserDto userDto = authenticationService.githubLogin(code);
		String tempJWT = "RETURNTESTJWT";
		return ResponseEntity.ok()
			.body(OauthResponse.of(userDto.getLogin(), userDto.getAvatar_url(), tempJWT));
	}
}
