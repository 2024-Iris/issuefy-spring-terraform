package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class NotificationDto {
	private Long userNotificationId;
	private String orgName;
	private String repositoryName;
	private LocalDateTime notificationCreatedAt;
	private boolean isRead;

	public static NotificationDto of(Long userNotificationId, String orgName, String repositoryName,
		LocalDateTime localDateTime,
		boolean isRead) {
		return new NotificationDto(userNotificationId, orgName, repositoryName, localDateTime, isRead);
	}
}
