package com.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class OrderIntegrationApp {

	public static void main(String[] args) {
		SpringApplication.run(OrderIntegrationApp.class, args);
	}

}
