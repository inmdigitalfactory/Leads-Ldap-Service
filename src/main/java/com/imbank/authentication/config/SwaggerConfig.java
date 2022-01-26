package com.imbank.authentication.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI initDocs() {
        Contact contact = new Contact();
        contact.setUrl("https://imbank.com");
        contact.setEmail("a@imbank.com");
        contact.setName("iCube");
        return new OpenAPI()
                .info(new Info().title("iCube LDAP Middleware")
                        .description("Microservice that allows applications to authenticate users via LDAP")
                        .version("v0.0.1")
                        .contact(contact))
                .components(new Components()
                        .addSecuritySchemes("Application Access Token", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .name("Authorization")
                                .scheme("Bearer")
                                .bearerFormat("Access Token")
                                .in(SecurityScheme.In.HEADER)
                                .description("App access token that allows app to use this service"))
                )
                ;
    }

}
