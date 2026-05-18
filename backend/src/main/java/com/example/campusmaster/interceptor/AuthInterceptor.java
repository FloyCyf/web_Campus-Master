package com.example.campusmaster.interceptor;

import com.example.campusmaster.common.BusinessException;
import com.example.campusmaster.common.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
        }

        if (token == null || token.isEmpty()) {
            throw BusinessException.unauthorized("请先登录");
        }

        try {
            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

            return true;
        } catch (Exception e) {
            throw BusinessException.unauthorized("登录已失效，请重新登录");
        }
    }
}
