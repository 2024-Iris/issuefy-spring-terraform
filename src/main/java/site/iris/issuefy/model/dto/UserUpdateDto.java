package site.iris.issuefy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserUpdateDto {
	private String email;
	private boolean alertStatus;
}
