
package com.mossle.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String desc() default "没有标准描述";

    boolean view() default true;

    String operationDesc() default "没有默认描述";

    String action() default "没有操作类型";
}