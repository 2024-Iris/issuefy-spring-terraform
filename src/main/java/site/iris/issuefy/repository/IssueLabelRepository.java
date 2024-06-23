package site.iris.issuefy.repository;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.IssueLabel;

public interface IssueLabelRepository extends CrudRepository<IssueLabel, Long> {
}
