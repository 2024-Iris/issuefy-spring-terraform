package site.iris.issuefy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatusController {

	@GetMapping("/status")
	@ResponseBody
	public String status() {
		return "service is running!";
	}

	public String sonar() {
		return "sonar is ok";
	}
}
