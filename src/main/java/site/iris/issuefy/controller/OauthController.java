package site.iris.issuefy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.dto.OauthResponse;
import site.iris.issuefy.service.OauthService;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OauthController {
	private final OauthService oauthService;

	@GetMapping("/api/login")
	public ResponseEntity<OauthResponse> login(@RequestParam String code) {
		oauthService.githubLogin(code);
		String tempUserName = "roy";
		String tempJWT = "SAD124i2SDF39AAS28349CIDOZPLKTMBIJGR";
		return ResponseEntity.ok()
			.body(OauthResponse.of(tempUserName, tempJWT));
	}
}
