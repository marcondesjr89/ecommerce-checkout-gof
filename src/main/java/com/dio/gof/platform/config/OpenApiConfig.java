package com.dio.gof.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Checkout API - GoF Design Patterns")
                        .version("1.0.0")
                        .description("""
                                REST API demonstrating Gang of Four (GoF) Design Patterns with Spring Boot 3 and Java 21:

                                - **Strategy:** Interchangeable payment (PIX, Credit Card, Boleto) and shipping (Express, Economy) algorithms.
                                - **Facade:** Unified CheckoutFacade orchestrating validation, shipping, payment, persistence, and auditing.
                                - **Singleton:** Thread-safe single instances managed by the Spring IoC Container.
                                """)
                        .contact(new Contact()
                                .name("Ecommerce Checkout GoF")
                                .url("https://github.com/marcondesjr89/ecommerce-checkout-gof"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
