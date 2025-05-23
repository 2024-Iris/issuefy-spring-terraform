package site.iris.issuefy.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.IssueStar;
import site.iris.issuefy.entity.User;

public interface IssueStarRepository extends CrudRepository<IssueStar, Long> {
	Optional<IssueStar> findByUserAndIssue(User user, Issue issue);
}
