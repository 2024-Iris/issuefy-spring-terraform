package site.iris.issuefy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Subscription;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
	Page<Subscription> findByUserId(Long userId, Pageable pageable);

	Optional<Subscription> findByUserIdAndRepository_GhRepoId(Long userId, Long ghRepoId);

	void deleteByRepository_GhRepoId(Long ghRepoId);

	List<Subscription> findByRepositoryId(Long repositoryId);
}
