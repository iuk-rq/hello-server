package com.stu.helloserver.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String method = request.getMethod();
        String uri = request.getRequestURI();


        boolean isCreateUser = "POST".equalsIgnoreCase(method) && "/api/users".equals(uri);
        boolean isGetUser = "GET".equalsIgnoreCase(method) && uri.startsWith("/api/users/");


        if (isCreateUser || isGetUser) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=UTF-8");
            String errorJson = "{\"code\": 401, \"msg\": \"非法操作:敏感动作["+method+"]需携带登录凭证\"}";
            PrintWriter writer = response.getWriter();
            writer.write(errorJson);
            writer.flush();
            writer.close();
            return false;
        }


        return true;
    }
}