package com.alvin.example.watchlog.advice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 是否敏感数据
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/12/9 18:22
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
}
