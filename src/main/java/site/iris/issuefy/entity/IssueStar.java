package site.iris.issuefy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_issue_star")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueStar {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "issue_id", nullable = false)
	private Issue issue;

	private IssueStar(User user, Issue issue) {
		this.user = user;
		this.issue = issue;
	}

	public static IssueStar of(User user, Issue issue) {
		return new IssueStar(user, issue);
	}
}