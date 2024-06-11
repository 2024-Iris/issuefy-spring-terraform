package site.iris.issuefy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.component.AmazonSQSSender;

@RestController
@RequiredArgsConstructor
public class Test {

	private final AmazonSQSSender sqsSender;

	@GetMapping("/api/sqs")
	public String test() {

		sqsSender.sendMessage("yeah");

		return "nice!";
	}
}
