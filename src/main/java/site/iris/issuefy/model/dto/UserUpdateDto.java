package site.iris.issuefy.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class UserUpdateDto {
	private String email;
	private boolean alertStatus;
}
