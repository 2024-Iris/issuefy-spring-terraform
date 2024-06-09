package site.iris.issuefy.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Repository;

public interface RepositoryRepository extends CrudRepository<Repository, Long> {
	Optional<Repository> findByNameAndOrgId(String repositoryName, Long orgId);

	Optional<Repository> findByGhRepoId(Long ghRepoId);
}
