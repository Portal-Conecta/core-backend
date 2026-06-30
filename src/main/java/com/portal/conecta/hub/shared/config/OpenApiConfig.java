package com.portal.conecta.hub.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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

    @Value("${portal.openapi.server-url:/}")
    private String serverUrl;

    @Value("${portal.openapi.auth-server-url:/}")
    private String authServerUrl;

    @Bean
    public OpenAPI hubOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url(serverUrl)))
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

    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/auth/**")
                .addOpenApiCustomizer(openApi ->
                        openApi.setServers(List.of(new Server().url(authServerUrl))))
                .build();
    }

    @Bean
    public GroupedOpenApi hubGroup() {
        return GroupedOpenApi.builder()
                .group("hub")
                .pathsToExclude("/auth/**")
                .addOpenApiCustomizer(openApi ->
                        openApi.setServers(List.of(new Server().url(serverUrl))))
                .build();
    }
}
