package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationDto {
	private String orgName;
	private String message;
	private LocalDateTime localDateTime;
	private boolean isRead;

	public static NotificationDto of(String orgName, String message, LocalDateTime localDateTime, boolean isRead) {
		return new NotificationDto(orgName, message, localDateTime, isRead);
	}

	@Override
	public String toString() {
		return "NotificationListDto{" +
			"orgName='" + orgName + '\'' +
			", message='" + message + '\'' +
			", localDateTime=" + localDateTime +
			", isRead=" + isRead +
			'}';
	}
}
