package site.iris.issuefy.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.iris.issuefy.dto.SubscribedResponse;
import site.iris.issuefy.service.SubscriptionService;
import site.iris.issuefy.vo.RepoVO;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
	private final SubscriptionService subscriptionService;

	public SubscriptionController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@GetMapping
	public ResponseEntity<List<SubscribedResponse>> getSubscribedRepositories() {
		List<SubscribedResponse> subscribedResponses = subscriptionService.getSubscribedRepositories();

		return ResponseEntity.ok(subscribedResponses);
	}

	@PostMapping
	public ResponseEntity<SubscribedResponse> create(@RequestBody RepoVO repoVO) {
		SubscribedResponse subscribedResponse = new SubscribedResponse();

		return ResponseEntity.created(URI.create("/subscriptions/" + subscribedResponse.getId())).body(
			subscribedResponse);
	}
}
