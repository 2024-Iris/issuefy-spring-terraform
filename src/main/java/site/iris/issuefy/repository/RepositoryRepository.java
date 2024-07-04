package site.iris.issuefy.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Repository;

public interface RepositoryRepository extends CrudRepository<Repository, Long> {
	Optional<Repository> findByGhRepoId(Long ghRepoId);

	Optional<Repository> findByName(String name);

	Boolean existsByName(String name);

	// Long findRepositoryIdByRepositoryName(String name);
}
