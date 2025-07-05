package com.reliaquest.api.component;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Thiis aspect will log entry and exit points of every public method intercepted by the pointcut along with the parameters and return values
 * It will also log any exception occurring on the method body.
 */
@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Pointcut: It will intercept any public method in any class inside com.reliaquest.api package
    @Pointcut("execution(public * com.reliaquest.api..*(..))")
    public void publicMethod() {}

    @Around("publicMethod()")
    public Object logEntryExit(ProceedingJoinPoint joinPoint) throws Throwable {
        final String methodName = joinPoint.getSignature().toShortString();

        // Get method arguments
        final Object[] args = joinPoint.getArgs();

        // Log entry
        logger.info("Entering method: {} with args: {}", methodName, Arrays.toString(args));

        try {
            final Object result = joinPoint.proceed();
            logger.info("Exiting method: {} with result: {}", methodName, result);

            return result;
        } catch (final Throwable t) {
            logger.error("Exception in: " + methodName, t);
            throw t;
        }
    }
}
