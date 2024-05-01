package site.iris.issuefy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.service.OauthService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OauthController {
	private final OauthService oauthService;

	@GetMapping("/api/login")
	public String login(@RequestParam String code) {
		oauthService.githubLogin(code);

		return "login success!";
	}
}
