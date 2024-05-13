package com.mztrade.hki.aspect;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Pointcut("execution (* com.mztrade.hki.service..*(..))")
    private void serviceFunctions(){}

    @Before("serviceFunctions()")
    public void before(JoinPoint joinPoint) { // JoinPoint 지점 정보
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();
        List<String> parameters = IntStream
                .range(0, parameterNames.length)
                .mapToObj(i -> String.format("%s: %s", parameterNames[i], parameterValues[i]))
                .toList();
        log.info(String.format("%s: %s(%s)", joinPoint.getSourceLocation().getWithinType().getName(), method.getName(), String.join(", ", parameters)));
    }
}
