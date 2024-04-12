package site.iris.issuefy.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import site.iris.issuefy.domain.User;
import site.iris.issuefy.dto.UserRequest;
import site.iris.issuefy.service.UserService;

@RestController
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/users")
	public ResponseEntity<Void> join(@RequestBody UserRequest userRequest) {
		User user = userService.create(userRequest);
		return ResponseEntity.created(URI.create("/users/" + user.getId())).build();
	}
}
