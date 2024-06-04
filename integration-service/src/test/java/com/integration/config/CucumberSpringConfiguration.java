package com.integration.config;

import io.cucumber.spring.CucumberContextConfiguration;
import com.integration.OrderIntegrationApp;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = OrderIntegrationApp.class)
public class CucumberSpringConfiguration {
}
