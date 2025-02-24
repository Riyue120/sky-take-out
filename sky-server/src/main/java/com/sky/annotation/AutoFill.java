package com.sky.annotation;

import com.sky.enumeration.OperationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
/// 标识需要自动填充的字段
public @interface AutoFill {
    // 数据库操作类型，UPDATE,INSERT
    OperationType value();

}
