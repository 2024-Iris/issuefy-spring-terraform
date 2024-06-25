package site.iris.issuefy.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class NotificationDto {
	private int unreadCount;
	private List<String> latestMessages;
}
