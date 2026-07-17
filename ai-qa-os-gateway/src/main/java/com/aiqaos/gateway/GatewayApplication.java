package com.aiqaos.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = "com.aiqaos"
)
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.aiqaos")
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "com.aiqaos")
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
