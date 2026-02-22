package com.noom.interview.fullstack.sleep;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sleepLoggerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sleep Logger API")
                        .description("API for the Noom sleep logger (create and fetch sleep logs, 30-day averages)")
                        .version("0.0.1"));
    }
}
