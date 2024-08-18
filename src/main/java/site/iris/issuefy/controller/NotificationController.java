package site.iris.issuefy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.model.dto.NotificationReadDto;
import site.iris.issuefy.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public ResponseEntity<List<NotificationDto>> getNotifications(@RequestAttribute String githubId) {
		List<NotificationDto> notificationDtos = notificationService.findNotifications(githubId);
		return ResponseEntity.ok(notificationDtos);
	}

	@PatchMapping
	public ResponseEntity<Void> updateNotification(@RequestBody NotificationReadDto notificationReadDto) {
		notificationService.updateUserNotificationsAsRead(notificationReadDto);
		return ResponseEntity.ok().build();
	}

}
