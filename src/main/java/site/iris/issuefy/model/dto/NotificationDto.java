package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationDto {
	private Long userNotificationId;
	private String orgName;
	private String message;
	private LocalDateTime localDateTime;
	private boolean isRead;

	public static NotificationDto of(Long userNotificationId, String orgName, String message, LocalDateTime localDateTime,
		boolean isRead) {
		return new NotificationDto(userNotificationId, orgName, message, localDateTime, isRead);
	}

	@Override
	public String toString() {
		return "NotificationDto{" +
			"userNotificationId=" + userNotificationId +
			", orgName='" + orgName + '\'' +
			", message='" + message + '\'' +
			", localDateTime=" + localDateTime +
			", isRead=" + isRead +
			'}';
	}
}
