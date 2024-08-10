package site.iris.issuefy.global;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ContainerIdUtil {
	private static final String METADATA_URI = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
	private static final String LOCAL_CONTAINER_ID = "local-dev-container";

	public static String getContainerId() {
		if (!EnvironmentUtil.isEcsEnvironment()) {
			return LOCAL_CONTAINER_ID;
		}

		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(METADATA_URI))
				.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			ObjectMapper mapper = new ObjectMapper();
			var containerInfo = mapper.readTree(response.body());
			return containerInfo.get("ContainerID").asText();
		} catch (Exception e) {
			return "unknown-ecs-container";
		}
	}
}