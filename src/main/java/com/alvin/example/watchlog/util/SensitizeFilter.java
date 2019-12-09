package com.alvin.example.watchlog.util;

import com.alibaba.fastjson.serializer.ValueFilter;
import com.alvin.example.watchlog.advice.annotation.Sensitive;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * <p>
 * 脱敏过滤器
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/12/9 18:29
 */
@Slf4j
public class SensitizeFilter implements ValueFilter {
    public static final String SENSITIVE_DATA = "******";

    @Override
    public Object process(Object object, String name, Object value) {
        if (!(value instanceof String) || ((String) value).length() == 0) {
            return value;
        }
        try {
            Field field = object.getClass().getDeclaredField(name);
            if (String.class == field.getType() && field.getAnnotation(Sensitive.class) != null) {
                return SENSITIVE_DATA;
            }
            return value;
        } catch (NoSuchFieldException e) {
            log.error("当前数据类型为{},值为{}", object.getClass(), value);
            return value;
        }
    }
}