package com.stu.helloserver.security;

import com.stu.helloserver.model.entity.User;
import com.stu.helloserver.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 关键：换成 jakarta.servlet 包
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取请求头中的 Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. 无 Token 直接放行
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 截取 Token 字符串
        String jwt = authHeader.substring(7);
        String username;

        try {
            // 解析 Token 获取用户名
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Token 解析失败，直接放行
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 用户名不为空且当前未认证时，进行认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从数据库查询用户
            User user = userMapper.selectByUsername(username);
            if (user != null) {
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, null);
                // 设置请求详情
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 存入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 继续执行过滤链
        filterChain.doFilter(request, response);
    }
}