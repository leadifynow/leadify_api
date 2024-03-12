package com.api.leadify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.api.leadify.jwt")
@ComponentScan("com.api.leadify.controller")
@ComponentScan("com.api.leadify.dao")
@ComponentScan("com.api.leadify.entity")
@ComponentScan("com.api.leadify.service")
public class LeadifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeadifyApplication.class, args);
	}

}
