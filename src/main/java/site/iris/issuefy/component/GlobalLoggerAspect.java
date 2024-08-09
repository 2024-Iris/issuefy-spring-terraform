package site.iris.issuefy.component;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class GlobalLoggerAspect {
	@Around("execution(* site.iris.issuefy.controller..*.*(..))")
	public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
		Class<?> clazz = joinPoint.getTarget().getClass();
		Logger logger = LoggerFactory.getLogger(clazz);

		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String methodName = joinPoint.getSignature().getName();

		logger.info("Request: {} {} - Method: {}",
			request.getMethod(), request.getRequestURI(), methodName);

		long startTime = System.currentTimeMillis();
		Object result = joinPoint.proceed();
		long duration = System.currentTimeMillis() - startTime;

		logger.info("Response: {} {} - Method: {} - Duration: {}ms",
			request.getMethod(), request.getRequestURI(), methodName, duration);

		return result;
	}

	@AfterThrowing(pointcut = "execution(* site.iris.issuefy.service..*.*(..))", throwing = "ex")
	public void logServiceException(JoinPoint joinPoint, Exception ex) {
		Class<?> clazz = joinPoint.getTarget().getClass();
		Logger logger = LoggerFactory.getLogger(clazz);

		String methodName = joinPoint.getSignature().getName();
		logger.error("Exception in method: {} - Error: {}",
			methodName, ex.getMessage(), ex);
	}
}
