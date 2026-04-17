package fr.eni.bookhub.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API BookHub",
        description = "Documentation de l'API pour l'application BookHub",
        version = "1.0"
))
public class OpenApiConfig {
}