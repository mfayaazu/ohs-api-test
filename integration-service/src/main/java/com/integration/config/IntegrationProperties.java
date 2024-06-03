package com.integration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    private String inputFilePath;
    private String outputFilePath;

}
