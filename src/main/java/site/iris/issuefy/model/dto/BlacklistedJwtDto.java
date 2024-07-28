package site.iris.issuefy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BlacklistedJwtDto {
	private String token;
	private LocalDateTime invalidatedAt;
	private LocalDateTime expiresAt;
}
