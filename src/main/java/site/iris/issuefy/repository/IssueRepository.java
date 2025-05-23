package site.iris.issuefy.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import io.lettuce.core.dynamic.annotation.Param;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.model.dto.IssueWithPagedDto;
import site.iris.issuefy.model.dto.IssueWithStarDto;

public interface IssueRepository extends CrudRepository<Issue, Long> {
	Optional<Page<Issue>> findAllByRepository_Id(Long id, Pageable pageable);

	@Query("SELECT i.ghIssueId FROM Issue i WHERE i.repository.id = :repositoryId")
	Set<Long> findGhIssueIdByRepositoryId(Long repositoryId);

	Optional<Issue> findByGhIssueId(Long githubId);

	Optional<Issue> findFirstByRepositoryIdOrderByUpdatedAtDesc(Long id);

	@Query("SELECT MAX(i.createdAt) FROM Issue i WHERE i.repository.id = :repoId")
	LocalDateTime getLatestCreatedAtByRepository_Id(@Param("repoId") Long repoId);

	@Query(
		"SELECT new site.iris.issuefy.model.dto.IssueWithPagedDto(i, CASE WHEN uis.id IS NOT NULL THEN true ELSE false END) "
			+
			"FROM Issue i " +
			"LEFT JOIN UserIssueStar uis ON i.id = uis.issue.id AND uis.user.id = :userId " +
			"WHERE i.repository.id = :repositoryId")
	Page<IssueWithPagedDto> findIssuesWithPaged(@Param("repositoryId") Long repositoryId,
		@Param("userId") Long userId,
		Pageable pageable);

	@Query("SELECT new site.iris.issuefy.model.dto.IssueWithStarDto(i, true, o.name, r.name) " +
		"FROM Issue i " +
		"JOIN i.repository r " +
		"JOIN r.org o " +
		"JOIN Subscription s ON s.repository = r " +
		"JOIN IssueStar is ON is.issue = i AND is.user.id = :userId " +
		"WHERE s.user.id = :userId " +
		"ORDER BY i.updatedAt DESC")
	List<IssueWithStarDto> findTop5StarredIssuesForUserWithLabels(@Param("userId") Long userId);
}
