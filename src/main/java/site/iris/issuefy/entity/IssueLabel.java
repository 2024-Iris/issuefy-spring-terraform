package site.iris.issuefy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "issue_label")
public class IssueLabel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "issue_id")
	@Setter
	private Issue issue;

	@ManyToOne
	@JoinColumn(name = "label_id")
	private Label label;

	private IssueLabel(Issue issue, Label label) {
		this.issue = issue;
		this.label = label;
	}

	public static IssueLabel of(Issue issue, Label label) {
		return new IssueLabel(issue, label);
	}
}
