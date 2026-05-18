package com.example.campusmaster.interceptor;

import com.example.campusmaster.annotation.RequiresRoles;
import com.example.campusmaster.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RoleInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        RequiresRoles requiresRoles = method.getAnnotation(RequiresRoles.class);
        if (requiresRoles == null) {
            return true;
        }

        String currentRole = (String) request.getAttribute("role");
        if (currentRole == null) {
            throw BusinessException.unauthorized("请先登录");
        }

        String[] allowedRoles = requiresRoles.value();
        boolean requireAll = requiresRoles.requireAll();

        if (requireAll) {
            for (String role : allowedRoles) {
                if (!role.equals(currentRole)) {
                    throw BusinessException.forbidden("权限不足");
                }
            }
        } else {
            boolean hasRole = false;
            for (String role : allowedRoles) {
                if (role.equals(currentRole)) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                throw BusinessException.forbidden("权限不足");
            }
        }

        return true;
    }
}
