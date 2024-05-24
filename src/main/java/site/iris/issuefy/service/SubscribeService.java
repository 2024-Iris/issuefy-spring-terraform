package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.dto.SubscribeResponse;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscribe;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.repository.OrgRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.repository.SubscribeRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.vo.OrgRecord;
import site.iris.issuefy.vo.RepositoryDto;
import site.iris.issuefy.vo.RepositoryUrlDto;
import site.iris.issuefy.vo.TokenDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscribeService {
	private final SubscribeRepository subscribeRepository;
	private final UserRepository userRepository;
	private final TokenProvider tokenProvider;
	private final OrgRepository orgRepository;
	private final RepositoryRepository repositoryRepository;

	public List<SubscribeResponse> getSubscribedRepositories(String token) {
		TokenDto tokenDto = TokenDto.fromClaims(tokenProvider.getClaims(token));
		User user = userRepository.findByGithubId(tokenDto.getGithubId()).orElseThrow();
		List<Subscribe> subscribes = subscribeRepository.findByUserId(user.getId());

		Map<OrgRecord, List<RepositoryDto>> orgResponseMap = new HashMap<>();
		for (Subscribe subscribe : subscribes) {
			Long orgId = subscribe.getRepository().getOrg().getId();
			String orgName = subscribe.getRepository().getOrg().getName();

			RepositoryDto repositoryDto = RepositoryDto.of(subscribe.getRepository().getId(),
				subscribe.getRepository().getName());

			OrgRecord orgRecord = OrgRecord.from(orgId, orgName, new ArrayList<>());

			if (!orgResponseMap.containsKey(orgRecord)) {
				orgResponseMap.put(orgRecord, new ArrayList<>());
			}
			orgResponseMap.get(orgRecord).add(repositoryDto);
		}

		List<SubscribeResponse> responses = new ArrayList<>();
		for (Map.Entry<OrgRecord, List<RepositoryDto>> entry : orgResponseMap.entrySet()) {
			OrgRecord orgRecord = OrgRecord.from(entry.getKey().id(), entry.getKey().name(), entry.getValue());
			responses.add(SubscribeResponse.from(orgRecord));
		}

		return responses;
	}

	@Transactional
	public void addSubscribeRepository(RepositoryUrlDto repositoryUrlDto) {
		try {
			Org org = orgRepository.findByName(repositoryUrlDto.getOrgName())
				.orElseGet(() -> {
					Org newOrg = new Org(repositoryUrlDto.getOrgName());
					return orgRepository.save(newOrg);
				});
			Repository repository = repositoryRepository.findByOrgIdAndName(org.getId(),
					repositoryUrlDto.getRepositoryName())
				.orElseGet(() -> {
					Repository newRepository = new Repository(org, repositoryUrlDto.getRepositoryName());
					return repositoryRepository.save(newRepository);
				});
			User user = userRepository.findByGithubId(repositoryUrlDto.getGithubId())
				.orElseGet(() -> {
					User newUser = new User(repositoryUrlDto.getGithubId());
					return userRepository.save(newUser);
				});
			Subscribe subscribe = subscribeRepository.findByUserIdAndRepositoryId(user.getId(), repository.getId())
				.orElseGet(() -> {
					Subscribe newSubscribe = new Subscribe(user, repository);
					return subscribeRepository.save(newSubscribe);
				});
		} catch (Exception exception) {
			log.error("save subscribe repository error", exception);
			throw new RuntimeException(exception);
		}
	}
}
