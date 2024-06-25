package site.iris.issuefy.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "notification")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository repository;

	@Column(name = "message", nullable = false)
	private String message;

	@Column(name = "push_time", nullable = false)
	private LocalDateTime pushTime;

	@Override
	public String toString() {
		return "Notification{" +
			"id=" + id +
			", repository=" + repository +
			", message='" + message + '\'' +
			", pushTime=" + pushTime +
			'}';
	}
}
