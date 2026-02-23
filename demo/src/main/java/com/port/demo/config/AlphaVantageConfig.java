package com.port.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "alphavantage.api")
@Data
public class AlphaVantageConfig {
    private String key;
    private String baseUrl;
}
