package site.iris.issuefy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import site.iris.issuefy.entity.Subscription;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
	Page<Subscription> findByUserId(Long userId, Pageable pageable);

	@Query("SELECT s FROM Subscription s JOIN s.repository r JOIN r.org " +
		"WHERE s.user.id = :userId")
	Page<Subscription> findPageByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT s FROM Subscription s JOIN s.repository r JOIN r.org " +
		"WHERE s.user.id = :userId AND s.repoStarred = true")
	Page<Subscription> findPageByUserIdAndRepoStarredTrue(@Param("userId") Long userId, Pageable pageable);

	Optional<Subscription> findByUserIdAndRepository_GhRepoId(Long userId, Long ghRepoId);

	void deleteByRepository_GhRepoId(Long ghRepoId);

	List<Subscription> findByRepositoryId(Long repositoryId);
}
