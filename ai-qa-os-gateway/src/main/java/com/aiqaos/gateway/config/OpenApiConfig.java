package com.aiqaos.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI aiQaOsOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AI-QA-OS API")
                .description("Enterprise Autonomous QA Platform API")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("AI-QA-OS Team")
                    .email("qaos@enterprise.com")))
            // Register the Bearer JWT security scheme
            .components(new Components()
                .addSecuritySchemes(BEARER_AUTH,
                    new SecurityScheme()
                        .name(BEARER_AUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter the JWT token obtained from POST /api/auth/login")))
            // Apply Bearer auth globally to all endpoints
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}