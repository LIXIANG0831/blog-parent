package com.at.blog.common.aop;


import java.lang.annotation.*;


//TYPE 代表可以修饰类
//METHOD 表示可以修饰方法
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {
    String module() default "";
    String operator() default "";
}
