package site.iris.issuefy.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import io.lettuce.core.dynamic.annotation.Param;
import site.iris.issuefy.entity.Issue;

public interface IssueRepository extends CrudRepository<Issue, Long> {
	Optional<Page<Issue>> findAllByRepository_Id(Long id, Pageable pageable);

	Optional<Issue> findFirstByRepositoryIdOrderByUpdatedAtDesc(Long id);

	@Query("SELECT MAX(i.createdAt) FROM Issue i WHERE i.repository.id = :repoId")
	LocalDateTime getLatestCreatedAtByRepository_Id(@Param("repoId") Long repoId);
}
