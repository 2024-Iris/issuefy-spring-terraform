package site.iris.issuefy.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Issue;

public interface IssueRepository extends CrudRepository<Issue, Long> {
	List<Issue> findAllByRepository_Id(Long id);

	@Query("SELECT MAX(i.createdAt) FROM Issue i")
	LocalDateTime getLatestCreatedAt();

	@Query("SELECT MAX(i.updatedAt) FROM Issue i")
	LocalDateTime getLatestUpdatedAt();
}
