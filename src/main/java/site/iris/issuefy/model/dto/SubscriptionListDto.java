package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SubscriptionListDto {
	private Long orgId;
	private String orgName;
	private Long githubRepositoryId;
	private String repositoryName;
	private LocalDateTime repositoryLatestUpdateAt;
	private boolean repositoryStarred;

	public static SubscriptionListDto of(
		Long orgId,
		String orgName,
		Long githubRepositoryId,
		String repositoryName,
		LocalDateTime repositoryLatestUpdateAt,
		boolean repositoryStarred
	) {
		return new SubscriptionListDto(orgId, orgName, githubRepositoryId, repositoryName, repositoryLatestUpdateAt,
			repositoryStarred);
	}
}
