package site.iris.issuefy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.service.NotificationService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("api/notifications")
	public ResponseEntity<List<NotificationDto>> getNotifications(@RequestAttribute String githubId) {
		List<NotificationDto> notificationDtos = notificationService.findNotifications(githubId);
		log.info(notificationDtos.toString());
		return ResponseEntity.ok(notificationService.findNotifications(githubId));
	}

}
