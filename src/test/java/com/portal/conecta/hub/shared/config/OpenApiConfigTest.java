package com.portal.conecta.hub.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void shouldDeclareServerUrlAsRootWhenPropertyIsSlash() {
        var config = buildConfig("/", "/");

        OpenAPI openAPI = config.hubOpenAPI();

        assertThat(openAPI.getServers()).hasSize(1);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/");
    }

    @Test
    void shouldDeclareServerUrlAsHubWhenPropertyIsHub() {
        var config = buildConfig("/hub", "/");

        OpenAPI openAPI = config.hubOpenAPI();

        assertThat(openAPI.getServers()).hasSize(1);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/hub");
    }

    @Test
    void shouldCreateAuthGroupMatchingAuthPaths() {
        var config = buildConfig("/", "/");

        var group = config.authGroup();

        assertThat(group).isNotNull();
        assertThat(group.getGroup()).isEqualTo("auth");
    }

    @Test
    void shouldCreateHubGroupExcludingAuthPaths() {
        var config = buildConfig("/hub", "/");

        var group = config.hubGroup();

        assertThat(group).isNotNull();
        assertThat(group.getGroup()).isEqualTo("hub");
    }

    @Test
    void shouldKeepAuthServerAsRootEvenInProdEnvironment() {
        var config = buildConfig("/hub", "/");

        var group = config.authGroup();

        assertThat(group).isNotNull();
        assertThat(group.getGroup()).isEqualTo("auth");
    }

    private OpenApiConfig buildConfig(String serverUrl, String authServerUrl) {
        var config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "serverUrl", serverUrl);
        ReflectionTestUtils.setField(config, "authServerUrl", authServerUrl);
        return config;
    }
}