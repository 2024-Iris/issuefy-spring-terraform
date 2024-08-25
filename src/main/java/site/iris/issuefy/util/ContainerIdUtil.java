package site.iris.issuefy.util;

import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ContainerIdUtil {
	private static final String METADATA_URI = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
	private static final String LOCAL_CONTAINER_ID = "local";
	private static final String UNKNOWN_CONTAINER_ID = "unknown";

	public static String getContainerId() {
		if (!isEcsEnvironment()) {
			return LOCAL_CONTAINER_ID;
		}

		try {
			String response = WebClient.create()
				.get()
				.uri(METADATA_URI)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(response);
			String containerId = jsonNode.path("Containers")
				.path(0)
				.path("DockerId")
				.asText(UNKNOWN_CONTAINER_ID);
			log.info("containerId: {}", containerId);
			return containerId;
		} catch (Exception e) {
			return UNKNOWN_CONTAINER_ID;
		}
	}

	public static boolean isEcsEnvironment() {
		return METADATA_URI != null && !METADATA_URI.isEmpty();
	}
}