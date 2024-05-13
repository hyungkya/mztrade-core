package com.mztrade.hki.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

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
        log.info(String.format("%s, %s", method.getName(), Arrays.stream(joinPoint.getArgs()).map(Object::toString).toList()));
    }
}
