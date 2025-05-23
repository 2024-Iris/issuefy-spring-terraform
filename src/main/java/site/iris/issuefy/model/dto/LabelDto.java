package site.iris.issuefy.model.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class LabelDto {
	Long id;
	String name;
	String color;
}


