package site.iris.issuefy.util;

public class EnvironmentUtil {
	public static boolean isEcsEnvironment() {
		return System.getenv("ECS_CONTAINER_METADATA_URI_V4") != null;
	}
}
