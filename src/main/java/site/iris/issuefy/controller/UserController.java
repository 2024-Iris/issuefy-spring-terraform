package site.iris.issuefy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.model.dto.UserDto;
import site.iris.issuefy.model.dto.UserUpdateDto;
import site.iris.issuefy.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

	private final UserService userService;

	@GetMapping("/info")
	public ResponseEntity<UserDto> getUserInfo(@RequestAttribute String githubId) {
		UserDto userDto = userService.getUserInfo(githubId);
		return ResponseEntity.ok().body(userDto);
	}

	@PatchMapping("/email")
	public ResponseEntity<String> updateEmail(@RequestAttribute String githubId,
		@RequestBody UserUpdateDto userUpdateDto) {
		userService.updateEmail(githubId, userUpdateDto.getEmail());
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/alert")
	public ResponseEntity<String> updateAlert(@RequestAttribute String githubId,
		@RequestBody UserUpdateDto userUpdateDto) {
		userService.updateAlert(githubId, userUpdateDto.isAlertStatus());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/withdraw")
	public ResponseEntity<String> withdraw(@RequestAttribute String githubId) {
		userService.withdraw(githubId);
		return ResponseEntity.ok().build();
	}
}
