package com.alvin.example.watchlog.advice;

import com.alvin.example.watchlog.advice.annotation.WatchLog;
import com.alvin.example.watchlog.util.ObjectViewUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * datetime 2019/12/6 22:26
 *
 * @author zhouwenxiang
 */
@Aspect
@Component
@Order(0)
@Slf4j
public class WatchLogAdvice {

    @Pointcut("@annotation(com.alvin.example.watchlog.advice.annotation.WatchLog)")
    public void targetPoint() {
    }

    @Around("targetPoint()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        Object[] args = jp.getArgs();
        Object returnObj = jp.proceed(args);

        // 保存原始数据，避免敏感数据脱敏时修改原始数据，导致返回前端的数据不同步
        Object old = returnObj.getClass().newInstance();
        BeanUtils.copyProperties(returnObj,old);

        MethodSignature signature = (MethodSignature) jp.getSignature();
        String className = jp.getSignature().getDeclaringType().getSimpleName();
        String methodName = jp.getSignature().getName();
        Method method = signature.getMethod();

        long cost = System.currentTimeMillis() - start;
        long trace = Thread.currentThread().getId();
        WatchLog[] watchLogAnnotations = method.getAnnotationsByType(WatchLog.class);
        for (WatchLog watchLogAnnotation : watchLogAnnotations) {
            int depth = watchLogAnnotation.expandDepth();
            boolean usingJson = watchLogAnnotation.isUsingJson();
            boolean printParentFields = watchLogAnnotation.isPrintParentFields();
            boolean ignoreNullField = watchLogAnnotation.isIgnoreNullField();
            ObjectViewUtil.ObjectViewConfig config = new ObjectViewUtil.ObjectViewConfig(depth, usingJson, printParentFields, ignoreNullField, true);
            StringBuilder logPlaceHolder = new StringBuilder("{} [{}] trace:{}, cost:{}");
            List<Object> logArgs = new ArrayList<>(args.length * 2 + 5);
            logArgs.add(className);
            logArgs.add(methodName);
            logArgs.add(trace);
            logArgs.add(cost);
            for (int i = 0; i < args.length; i++) {
                logPlaceHolder.append(", P{}:{}");
                logArgs.add(i + 1);
                logArgs.add(ObjectViewUtil.draw(args[i], config));
            }
            if (!(returnObj instanceof Void)) {
                logPlaceHolder.append(", R:{}");
                logArgs.add(ObjectViewUtil.draw(returnObj, config));
            }
            log.info(logPlaceHolder.toString(), logArgs.toArray());
        }

        return old;
    }

}
