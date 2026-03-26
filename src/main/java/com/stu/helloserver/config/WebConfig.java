package com.stu.helloserver.config;

import com.stu.helloserver.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")          // 拦截所有 /api 开头的接口
                .excludePathPatterns(
                        "/api/users",                // 放行注册接口
                        "/api/users/login"           // 放行登录接口
                );
//                .addPathPatterns("/api/**") // 拦截/api下所有请求
//                .excludePathPatterns("/api/users/login"); // 仅放行登录接口，其余均由拦截器内部判断
    }
}