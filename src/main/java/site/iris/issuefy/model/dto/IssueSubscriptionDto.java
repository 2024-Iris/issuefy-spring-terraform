package site.iris.issuefy.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.IssueLabel;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.entity.Repository;

@Data
@Getter
@AllArgsConstructor
public class IssueSubscriptionDto {
	Repository repository;
	List<IssueDto> issueDtos;
	List<Issue> updatedIssues;
	List<Label> allLabels;
	List<IssueLabel> issueLabels;
}
