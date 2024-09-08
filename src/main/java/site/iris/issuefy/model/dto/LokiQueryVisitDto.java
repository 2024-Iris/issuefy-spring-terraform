package site.iris.issuefy.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LokiQueryVisitDto {
	private String visitCount = "0";

	@JsonProperty("data")
	private void unpackData(Data data) {
		if (data.getResult() != null && !data.getResult().isEmpty()) {
			visitCount = data.getResult().get(0).getValue().get(1);
		}
	}

	@lombok.Data
	private static class Data {
		private List<Result> result;
	}

	@lombok.Data
	private static class Result {
		private List<String> value;
	}
}
