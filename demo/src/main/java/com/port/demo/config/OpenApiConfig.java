package com.port.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI portfolioOptimizerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Optimizer API")
                        .description("REST API for Portfolio Optimization based on MAD (Mean Absolute Deviation) Linearization. "
                                + "Manage users, assets, portfolios, and run portfolio optimizations using linear programming.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Portfolio Optimizer Team"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")));
    }
}
