package site.iris.issuefy.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Repository;

public interface RepositoryRepository extends CrudRepository<Repository, Long> {
	Optional<Repository> findByOrgIdAndName(Long orgId, String repositoryName);
}
