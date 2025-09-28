package com.example.bidding.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bidding WebSocket API")
                        .description("A Spring Boot application that provides a REST API for bidding with real-time WebSocket updates. " +
                                   "This API allows users to create auction items, place bids, and receive live updates through WebSocket connections.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bidding API Team")
                                .email("support@biddingapi.com")
                                .url("https://github.com/CodeApeAlpha/spring-boot-bidding-websocket-api"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.biddingapp.com")
                                .description("Production Server")
                ));
    }
}
