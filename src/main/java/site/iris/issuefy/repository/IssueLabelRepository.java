package site.iris.issuefy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.IssueLabel;

public interface IssueLabelRepository extends CrudRepository<IssueLabel, Long> {
	Optional<List<IssueLabel>> findByIssueId(Long issueId);
}
