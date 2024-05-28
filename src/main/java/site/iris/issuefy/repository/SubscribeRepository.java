package site.iris.issuefy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Subscribe;

public interface SubscribeRepository extends CrudRepository<Subscribe, Long> {
	List<Subscribe> findByUserId(Long userId);

	Optional<Subscribe> findByUserIdAndRepositoryId(Long userId, Long repositoryId);
}
