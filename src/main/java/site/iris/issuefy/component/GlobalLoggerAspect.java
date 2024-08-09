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
        String githubId = (String) request.getAttribute("githubId");

        logger.info("{} Request: {} {} - Method: {} - GithubID: {}",
            controllerType, request.getMethod(), request.getRequestURI(), methodName, githubId);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        if (controllerType.equals("SSE") && methodName.equals("connect")) {
            logger.info("SSE Connection established for user: {}", githubId);
        }

        logger.info("{} Response: {} {} - Method: {} - GithubID: {} - Duration: {}ms",
            controllerType, request.getMethod(), request.getRequestURI(), methodName, githubId, duration);

        return result;
    }

    @AfterThrowing(pointcut = "execution(* site.iris.issuefy.service..*.*(..))", throwing = "ex")
    public void logServiceException(JoinPoint joinPoint, Exception ex) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String methodName = joinPoint.getSignature().getName();
        logger.error("Exception in method: {} - Error: {}", methodName, ex.getMessage(), ex);
    }
}