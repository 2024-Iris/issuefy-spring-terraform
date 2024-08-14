package site.iris.issuefy.component;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class GlobalLoggerAspect {

	@Around("execution(* site.iris.issuefy.controller..*.*(..)) && !execution(* site.iris.issuefy.controller.SseController.*(..))")
	public Object logRegularController(ProceedingJoinPoint joinPoint) throws Throwable {
		return logController(joinPoint, "Regular");
	}

	@Around("execution(* site.iris.issuefy.controller.SseController.*(..))")
	public Object logSseController(ProceedingJoinPoint joinPoint) throws Throwable {
		return logController(joinPoint, "SSE");
	}

	private Object logController(ProceedingJoinPoint joinPoint, String controllerType) throws Throwable {
		Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String methodName = joinPoint.getSignature().getName();

		if (controllerType.equals("SSE") && methodName.equals("connect")) {
			logger.info("SSE Connection opened for user: {}", MDC.get("user"));
			return joinPoint.proceed();
		}

		logger.info("Request: {} {} - Method: {}",
			request.getMethod(), request.getRequestURI(), methodName);

		long startTime = System.currentTimeMillis();
		Object result = joinPoint.proceed();
		long duration = System.currentTimeMillis() - startTime;

		int statusCode = 200;
		if (result instanceof ResponseEntity) {
			statusCode = ((ResponseEntity<?>)result).getStatusCode().value();
		}

		logger.info("Response: {} {} {} - Method: {} - Duration: {}ms",
			statusCode, request.getMethod(), request.getRequestURI(), methodName, duration);

		return result;
	}

	@AfterThrowing(pointcut = "execution(* site.iris.issuefy..*.*(..))", throwing = "ex")
	public void logServiceException(JoinPoint joinPoint, Exception ex) {
		Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
		String methodName = joinPoint.getSignature().getName();
		logger.error("Exception in method: {} - Error: {}", methodName, ex.getMessage(), ex);
	}

	@Around("execution(* site.iris.issuefy.service..*.github*(..))")
	public Object logGithubApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
		Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
		String methodName = joinPoint.getSignature().getName();

		logger.info("GitHub API Call - Start: {}", methodName);
		long startTime = System.currentTimeMillis();

		Object result = joinPoint.proceed();

		long duration = System.currentTimeMillis() - startTime;
		logger.info("GitHub API Call - End: {} - Duration: {}ms", methodName, duration);

		return result;
	}
}