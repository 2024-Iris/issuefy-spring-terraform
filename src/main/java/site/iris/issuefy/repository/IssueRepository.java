package site.iris.issuefy.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import io.lettuce.core.dynamic.annotation.Param;
import site.iris.issuefy.entity.Issue;

public interface IssueRepository extends CrudRepository<Issue, Long> {
	List<Issue> findAllByRepository_Id(Long id);

	@Query("SELECT MAX(i.createdAt) FROM Issue i WHERE i.repository.id = :repoId")
	LocalDateTime getLatestCreatedAtByRepository_Id(@Param("repoId") Long repoId);

	// @Query("SELECT MAX(i.updatedAt) FROM Issue i")
	// LocalDateTime getLatestUpdatedAt();
}
