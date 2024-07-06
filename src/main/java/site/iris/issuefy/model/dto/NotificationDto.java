package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationDto {
	private Long userNotificationId;
	private String orgName;
	private String repositoryName;
	private LocalDateTime localDateTime;
	private boolean isRead;

	public static NotificationDto of(Long userNotificationId, String orgName, String repositoryName,
		LocalDateTime localDateTime,
		boolean isRead) {
		return new NotificationDto(userNotificationId, orgName, repositoryName, localDateTime, isRead);
	}

	@Override
	public String toString() {
		return "NotificationDto{" +
			"userNotificationId=" + userNotificationId +
			", orgName='" + orgName + '\'' +
			", repositoryName='" + repositoryName + '\'' +
			", localDateTime=" + localDateTime +
			", isRead=" + isRead +
			'}';
	}
}
