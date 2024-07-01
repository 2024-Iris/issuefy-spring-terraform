package site.iris.issuefy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class UnreadNotificationDto {
	private int unreadCount;
}
