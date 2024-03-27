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
        registry.addMapping("/*")
        .allowedOrigins("*")
        .allowCredentials(false)
        .allowedHeaders("*")
        .exposedHeaders("*")
        .allowedMethods("*")
        ;
        // LOGGER.info("<-------");
    }
}
