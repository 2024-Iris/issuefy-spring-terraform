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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "notification")
@ToString
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository repository;

	@Column(name = "repository_name", nullable = false)
	private String repositoryName;

	@Column(name = "push_time", nullable = false)
	private LocalDateTime pushTime;

	public Notification(Repository repository, String repositoryName, LocalDateTime pushTime) {
		this.repository = repository;
		this.repositoryName = repositoryName;
		this.pushTime = pushTime;
	}
}
