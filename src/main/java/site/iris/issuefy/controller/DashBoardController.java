package site.iris.issuefy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import site.iris.issuefy.response.DashBoardResponse;
import site.iris.issuefy.service.DashBoardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashBoardController {
	private final DashBoardService dashBoardService;

	@GetMapping
	public Mono<ResponseEntity<DashBoardResponse>> dashboard(@RequestAttribute String githubId) {
		return dashBoardService.getDashBoardFromLoki(githubId)
			.map(dashBoardResponse -> ResponseEntity.ok().body(dashBoardResponse))
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}
}
