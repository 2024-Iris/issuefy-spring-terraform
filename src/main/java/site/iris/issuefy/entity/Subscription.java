package site.iris.issuefy.entity;

import jakarta.persistence.CascadeType;
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
@Table(name = "subscription")
@ToString
public class Subscription {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository repository;

	@Column(name = "is_repo_starred", nullable = false)
	private boolean isRepoStarred;

	public Subscription(User user, Repository repository) {
		this.user = user;
		this.repository = repository;
		this.isRepoStarred = false;
	}

	public void toggleStar() {
		this.isRepoStarred = !this.isRepoStarred;
	}
}
