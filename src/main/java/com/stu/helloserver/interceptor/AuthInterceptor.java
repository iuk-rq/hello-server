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

        // 1. 放行：注册接口
        boolean isCreateUser = "POST".equalsIgnoreCase(method) && "/api/users".equals(uri);

        // 2. 放行：分页查询接口
        boolean isUserPage = "GET".equalsIgnoreCase(method) && "/api/users/page".equals(uri);

        // 3. 放行：用户详情查询接口（GET）
        boolean isGetDetail = "GET".equalsIgnoreCase(method) && uri.matches("/api/users/\\d+/detail");

        // 4. 放行用户信息更新接口（PUT）
        boolean isUpdateDetail = "PUT".equalsIgnoreCase(method) && uri.matches("/api/users/\\d+/detail");

        // 满足任意一个就放行
        if (isCreateUser || isUserPage || isGetDetail || isUpdateDetail) {
            return true;
        }

        // 其他接口需要 token
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