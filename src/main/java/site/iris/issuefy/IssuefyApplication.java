package site.iris.issuefy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class IssuefyApplication {

	public static void main(String[] args) {
		SpringApplication.run(IssuefyApplication.class, args);
	}

}
