package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Subscribe;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.repository.SubscribeRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.dto.SubscribeResponse;
import site.iris.issuefy.vo.OrgRecord;
import site.iris.issuefy.vo.RepositoryDto;
import site.iris.issuefy.vo.TokenDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService {
	private final SubscribeRepository subscribeRepository;
	private final UserRepository userRepository;
	private final TokenProvider tokenProvider;

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
}
