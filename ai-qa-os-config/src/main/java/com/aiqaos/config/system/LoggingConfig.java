package com.aiqaos.config.system;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.IOException;
import java.util.UUID;

@Configuration
public class LoggingConfig implements WebMvcConfigurer, Filter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String REQUEST_ID_KEY = "requestId";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) {
                String traceId = request.getHeader("X-Trace-Id");
                String requestId = request.getHeader("X-Request-Id");

                if (traceId == null || traceId.isEmpty()) {
                    traceId = UUID.randomUUID().toString();
                }
                if (requestId == null || requestId.isEmpty()) {
                    requestId = UUID.randomUUID().toString();
                }

                MDC.put(TRACE_ID_KEY, traceId);
                MDC.put(REQUEST_ID_KEY, requestId);
                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler, Exception ex) {
                MDC.clear();
            }
        });
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            String traceId = UUID.randomUUID().toString();
            String requestId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID_KEY, traceId);
            MDC.put(REQUEST_ID_KEY, requestId);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
        }
    }
}