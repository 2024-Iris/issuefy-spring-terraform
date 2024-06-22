package site.iris.issuefy.repository;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Issue;

public interface IssueRepository extends CrudRepository<Issue, Long> {
}
