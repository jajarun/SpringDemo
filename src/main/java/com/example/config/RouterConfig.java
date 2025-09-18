package com.example.config;

import com.example.handler.PublicHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.nest;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.RequestPredicates.*;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> publicRoutes(PublicHandler publicHandler) {
        return nest(path("/api/v1/public"),  // 公共API路由分组
            route(GET("/health"), publicHandler::health)
                .andRoute(GET("/info"), publicHandler::info)
                .andRoute(GET("/version"), publicHandler::version)
        );
    }

    // @Bean
    // public RouterFunction<ServerResponse> authRoutes(PublicHandler publicHandler) {
    //     return nest(path("/api/v1/auth"),  // 认证相关路由分组
    //         route(POST("/login"), publicHandler::login)
    //             .andRoute(POST("/logout"), publicHandler::logout)
    //             .andRoute(GET("/profile"), publicHandler::profile)
    //     );
    // }
} 