package com.integration.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import com.integration.OrderIntegrationApp;

@CucumberContextConfiguration
@SpringBootTest(classes = OrderIntegrationApp.class)
public class CucumberSpringConfiguration {
}
