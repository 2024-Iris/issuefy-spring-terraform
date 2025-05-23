package site.iris.issuefy.util;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Getter
@Slf4j
public class ContainerIdUtil {

	private static final String METADATA_FILE_PATH = System.getenv("ECS_CONTAINER_METADATA_FILE");
	private static final String LOCAL_CONTAINER_ID = "local";
	private static final String UNKNOWN_CONTAINER_ID = "unknown";
	private static final int CONTAINER_ID_LENGTH = 8;

	public static String containerId;

	@PostConstruct
	private void init() {
		containerId = fetchContainerId();
	}

	private String fetchContainerId() {
		if (!isEcsEnvironment()) {
			return LOCAL_CONTAINER_ID;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(new File(METADATA_FILE_PATH));
			String containerId = jsonNode.path("ContainerID").asText(UNKNOWN_CONTAINER_ID);
			if (!containerId.equals(UNKNOWN_CONTAINER_ID)) {
				return containerId.substring(0, CONTAINER_ID_LENGTH);
			}
		} catch (IOException e) {
			log.error("Error fetching container ID: ", e);
		}
		return UNKNOWN_CONTAINER_ID;
	}

	private boolean isEcsEnvironment() {
		return METADATA_FILE_PATH != null && !METADATA_FILE_PATH.isEmpty();
	}
}