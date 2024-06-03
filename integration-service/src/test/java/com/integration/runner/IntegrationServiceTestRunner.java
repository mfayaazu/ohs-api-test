package com.integration.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.integration.stepdefinitions", "com.integration.config"},
        plugin = {"pretty", "html:target/cucumber-reports.html"},
        monochrome = true
)
public class IntegrationServiceTestRunner {
}
