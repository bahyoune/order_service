package com.microtest.OrderService.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignJwtRelayConfig {

    @Bean
    public RequestInterceptor jwtRelayInterceptor() {
        return requestTemplate -> {
            var attrs = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attrs != null) {
                String header = attrs.getRequest().getHeader("Authorization");
                if (header != null && !header.isBlank()) {
                    requestTemplate.header("Authorization", header);
                }
            }

        };
    }
}
