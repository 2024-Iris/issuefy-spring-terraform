package site.iris.issuefy.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "issue")
@ToString
public class Issue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository repository;

	@Column(name = "title")
	private String title;

	@Column
	private boolean isRead;

	@Column
	private String state;

	@Column
	private LocalDateTime createdAt;

	@Column
	private LocalDateTime updatedAt;

	@Column
	private LocalDateTime closedAt;

	@Column
	private long ghIssueId;

	@OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IssueLabel> issueLabels;

	private Issue(Repository repository, String title, boolean isRead, String state, LocalDateTime createdAt,
		LocalDateTime updatedAt, LocalDateTime closedAt, long ghIssueId, List<IssueLabel> issueLabels) {
		this.repository = repository;
		this.title = title;
		this.isRead = isRead;
		this.state = state;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.closedAt = closedAt;
		this.ghIssueId = ghIssueId;
		this.issueLabels = issueLabels;
	}

	public static Issue of(Repository repository, String title, boolean isRead, String state, LocalDateTime createdAt,
		LocalDateTime updatedAt, LocalDateTime closedAt, long ghIssueNumber, List<IssueLabel> issueLabels) {
		return new Issue(repository, title, isRead, state, createdAt, updatedAt, closedAt, ghIssueNumber, issueLabels);
	}
}

