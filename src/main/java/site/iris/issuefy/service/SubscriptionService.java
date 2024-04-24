package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import site.iris.issuefy.dto.SubscribedResponse;

@Service
public class SubscriptionService {

	public List<SubscribedResponse> getSubscribedRepositories() {
		SubscribedResponse subscribedResponse = new SubscribedResponse();
		List<SubscribedResponse> subscribedResponses = new ArrayList<>();
		subscribedResponses.add(subscribedResponse);

		return subscribedResponses;
	}
}
