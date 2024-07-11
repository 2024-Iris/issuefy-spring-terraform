package site.iris.issuefy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_notification")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserNotification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "notification_id", nullable = false)
	private Notification notification;

	@Column(name = "is_read", nullable = false)
	private Boolean isRead;

	public UserNotification(User user, Notification notification) {
		this.user = user;
		this.notification = notification;
		this.isRead = false;
	}
}
