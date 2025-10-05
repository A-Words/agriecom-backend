package net.awords.agriecombackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agriecomOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Agriecom Backend API")
                        .description("农产品电商平台后端 API 文档")
                        .version("v1")
                        .contact(new Contact().name("Agriecom Team").email("dev@agriecom.local"))
                        .license(new License().name("MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Docs")
                        .url("https://example.com/docs"));
    }
}
