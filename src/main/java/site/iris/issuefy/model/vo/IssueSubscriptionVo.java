package site.iris.issuefy.model.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.IssueLabel;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.model.dto.IssueDto;

@Data
public class IssueSubscriptionVo {
	private final Repository repository;
	private final Optional<List<IssueDto>> optionalIssueDtos;
	private List<Issue> updatedIssues;
	private List<Label> allLabels;
	private List<IssueLabel> issueLabels;

	public IssueSubscriptionVo(Repository repository, Optional<List<IssueDto>> optionalIssueDtos) {
		this.repository = repository;
		this.optionalIssueDtos = optionalIssueDtos;
		this.updatedIssues = new ArrayList<>();
		this.allLabels = new ArrayList<>();
		this.issueLabels = new ArrayList<>();
	}
}
