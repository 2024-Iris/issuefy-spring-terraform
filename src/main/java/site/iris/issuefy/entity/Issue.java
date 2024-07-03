package site.iris.issuefy.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

@Entity
@NoArgsConstructor
@Getter
@Table(name = "issue")
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
	private boolean isStarred;

	@Column
	private boolean isRead;

	@Column
	private String state;

	@Column
	private Date createdAt;

	@Column
	private Date updatedAt;

	@Column
	private Date closedAt;

	@Column
	private long ghIssueId;

	@OneToMany(mappedBy = "issue")
	private List<IssueLabel> issueLabels = new ArrayList<>();

	private Issue(Repository repository, String title, boolean isStarred, boolean isRead, String state, Date createdAt,
		Date updatedAt, Date closedAt, long ghIssueId, List<IssueLabel> issueLabels) {
		this.repository = repository;
		this.title = title;
		this.isStarred = isStarred;
		this.isRead = isRead;
		this.state = state;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.closedAt = closedAt;
		this.ghIssueId = ghIssueId;
		this.issueLabels = issueLabels;
	}

	public static Issue of(Repository repository, String title, boolean isStarred, boolean isRead, String state,
		Date createdAt,
		Date updatedAt, Date closedAt, long ghIssueNumber, List<IssueLabel> issueLabels) {
		return new Issue(repository, title, isStarred, isRead, state, createdAt, updatedAt, closedAt, ghIssueNumber,
			issueLabels);
	}
}

