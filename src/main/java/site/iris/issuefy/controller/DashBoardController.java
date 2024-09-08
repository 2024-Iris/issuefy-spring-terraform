package site.iris.issuefy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.response.DashBoardResponse;
import site.iris.issuefy.service.DashBoardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashBoardController {
	private final DashBoardService dashBoardService;

	@GetMapping
	public ResponseEntity<DashBoardResponse> dashboard(@RequestAttribute String githubId) {
		DashBoardResponse dashBoardResponse = dashBoardService.getDashBoardFromLoki(githubId);
		return ResponseEntity.ok().body(dashBoardResponse);
	}
}
