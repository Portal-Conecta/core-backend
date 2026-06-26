package com.portal.conecta.hub.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura a documentação OpenAPI do Hub Core com esquema de autenticação Bearer JWT.
 *
 * <p>O esquema registrado sob {@link #BEARER_AUTH_SCHEME} deve ser referenciado
 * nas anotações {@code @SecurityRequirement} dos endpoints protegidos para que
 * o Swagger UI exiba o cadeado e permita envio do token nas requisições de teste.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI hubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hub Core API")
                        .description("Documentação navegável dos contratos HTTP do Hub Core.")
                        .version("0.0.1-SNAPSHOT"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
