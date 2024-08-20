package com.api.leadify.jwt;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // LOGGER.info("CORS POLICY on WebConfig!!!");
        registry.addMapping("/**")  // Apply to all endpoints
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")  // Add all your localhost origins here
                .allowCredentials(true)  // Allow credentials (cookies, authorization headers)
                .allowedHeaders("*")  // Allow all headers
                .exposedHeaders("*")  // Expose all headers
                .allowedMethods("*");  // Allow all HTTP methods

        // LOGGER.info("<-------");
    }
}
