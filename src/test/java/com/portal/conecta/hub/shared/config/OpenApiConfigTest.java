package com.portal.conecta.hub.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void shouldDeclareServerUrlAsRootWhenPropertyIsSlash() {
        var config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "serverUrl", "/");

        OpenAPI openAPI = config.hubOpenAPI();

        assertThat(openAPI.getServers()).hasSize(1);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/");
    }

    @Test
    void shouldDeclareServerUrlAsHubWhenPropertyIsHub() {
        var config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "serverUrl", "/hub");

        OpenAPI openAPI = config.hubOpenAPI();

        assertThat(openAPI.getServers()).hasSize(1);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/hub");
    }
}