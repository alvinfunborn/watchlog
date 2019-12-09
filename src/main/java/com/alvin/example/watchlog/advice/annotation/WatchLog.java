package com.alvin.example.watchlog.advice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * datetime 2019/12/6 22:23
 * watch log
 *
 * @author zhouwenxiang
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WatchLog {

    /**
     * 对象展开层数
     */
    int expandDepth() default 1;

    /**
     * 是否使用fastjson序列化
     */
    boolean isUsingJson() default false;

    /**
     * 是否打印父类属性
     */
    boolean isPrintParentFields() default true;

    /**
     * 是否忽略null属性
     */
    boolean isIgnoreNullField() default true;
}
