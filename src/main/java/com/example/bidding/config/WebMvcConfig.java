package com.example.bidding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * 
 * Configures the landing page as the default home page.
 * When users visit http://localhost:8080/, they will see landing.html
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Set landing page as the home page (highest priority)
        registry.addViewController("/")
                .setViewName("forward:/landing.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}

