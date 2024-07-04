package site.iris.issuefy.model.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class NotificationReadDto {
	List<Long> userNotificationIds;
}
