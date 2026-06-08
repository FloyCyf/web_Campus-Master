
package com.example.campusmaster.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * 用于标注需要特定角色才能访问的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRoles {

    /**
     * 允许的角色列表
     */
    String[] value() default {};

    /**
     * 是否需要全部角色
     */
    boolean requireAll() default false;
}
