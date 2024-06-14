package site.iris.issuefy.entity;

import jakarta.persistence.CascadeType;
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
@Table(name = "subscription")
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

	public Subscription(User user, Repository repository) {
		this.user = user;
		this.repository = repository;
	}

	@Override
	public String toString() {
		return "Subscription{" +
			"id=" + id +
			", user=" + user +
			", repository=" + repository +
			'}';
	}
}
