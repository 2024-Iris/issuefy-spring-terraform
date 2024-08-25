package site.iris.issuefy.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import io.lettuce.core.dynamic.annotation.Param;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.model.dto.IssueWithStarStatusDto;

public interface IssueRepository extends CrudRepository<Issue, Long> {
	Optional<Page<Issue>> findAllByRepository_Id(Long id, Pageable pageable);

	Optional<Issue> findFirstByRepositoryIdOrderByUpdatedAtDesc(Long id);

	@Query("SELECT MAX(i.createdAt) FROM Issue i WHERE i.repository.id = :repoId")
	LocalDateTime getLatestCreatedAtByRepository_Id(@Param("repoId") Long repoId);

	@Query(
		"SELECT new site.iris.issuefy.model.dto.IssueWithStarStatusDto(i, CASE WHEN uis.id IS NOT NULL THEN true ELSE false END) "
			+
			"FROM Issue i " +
			"LEFT JOIN UserIssueStar uis ON i.id = uis.issue.id AND uis.user.id = :userId " +
			"WHERE i.repository.id = :repositoryId")
	Page<IssueWithStarStatusDto> findIssuesWithStarStatus(@Param("repositoryId") Long repositoryId,
		@Param("userId") Long userId,
		Pageable pageable);
}
