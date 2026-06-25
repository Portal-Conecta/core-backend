package com.portal.conecta.hub.shared.observability.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(MockitoExtension.class)
class AccessLogFilterTest {

    @Mock
    private AuthenticatedUserLogResolver userLogResolver;

    /**
     * Subclasse do AccessLogFilter que captura o MDC antes de limpá-lo.
     * Sobrescreve o comportamento de limpeza para interceptar os valores.
     */
    class CapturingAccessLogFilter extends AccessLogFilter {
        final Map<String, String> captured = new HashMap<>();

        CapturingAccessLogFilter() {
            super(userLogResolver, new LoggingProperties("X-Correlation-Id", 128, true));
        }

        @Override
        protected void afterLog() {
            captured.put(LoggingContextKeys.METHOD, MDC.get(LoggingContextKeys.METHOD));
            captured.put(LoggingContextKeys.PATH, MDC.get(LoggingContextKeys.PATH));
            captured.put(LoggingContextKeys.STATUS, MDC.get(LoggingContextKeys.STATUS));
            captured.put(LoggingContextKeys.DURATION_MS, MDC.get(LoggingContextKeys.DURATION_MS));
            captured.put(LoggingContextKeys.USER_ID, MDC.get(LoggingContextKeys.USER_ID));
        }
    }

    private CapturingAccessLogFilter filter;

    private MockMvc buildMockMvc() {
        filter = new CapturingAccessLogFilter();
        return MockMvcBuilders
                .standaloneSetup(new StubController())
                .addFilter(filter)
                .build();
    }

    @Test
    @DisplayName("deve popular MDC com method ao executar a requisição")
    void shouldPopulateMdcWithMethod() throws Exception {
        Mockito.when(userLogResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.METHOD)).isEqualTo("GET");
    }

    @Test
    @DisplayName("deve popular MDC com path sem query string")
    void shouldPopulateMdcWithPathWithoutQueryString() throws Exception {
        Mockito.when(userLogResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub?token=secret&foo=bar")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.PATH)).isEqualTo("/stub");
        assertThat(filter.captured.get(LoggingContextKeys.PATH)).doesNotContain("token");
        assertThat(filter.captured.get(LoggingContextKeys.PATH)).doesNotContain("secret");
    }

    @Test
    @DisplayName("deve popular MDC com status da resposta")
    void shouldPopulateMdcWithStatus() throws Exception {
        Mockito.when(userLogResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.STATUS)).isEqualTo("200");
    }

    @Test
    @DisplayName("deve popular MDC com durationMs")
    void shouldPopulateMdcWithDurationMs() throws Exception {
        Mockito.when(userLogResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.DURATION_MS)).isNotNull();
        assertThat(Long.parseLong(filter.captured.get(LoggingContextKeys.DURATION_MS))).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("deve omitir userId no MDC quando requisição é anônima")
    void shouldOmitUserIdWhenAnonymous() throws Exception {
        Mockito.when(userLogResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.USER_ID)).isNull();
    }

    @Test
    @DisplayName("deve incluir userId no MDC quando usuário está autenticado")
    void shouldIncludeUserIdWhenAuthenticated() throws Exception {
        Mockito.when(userLogResolver.resolve()).thenReturn(Optional.of("user-uuid-123"));
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.USER_ID)).isEqualTo("user-uuid-123");
    }

    @Test
    @DisplayName("deve limpar chaves do MDC ao final da requisição")
    void shouldClearMdcAfterRequest() throws Exception {
        Mockito.when(userLogResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(MDC.get(LoggingContextKeys.METHOD)).isNull();
        assertThat(MDC.get(LoggingContextKeys.PATH)).isNull();
        assertThat(MDC.get(LoggingContextKeys.STATUS)).isNull();
        assertThat(MDC.get(LoggingContextKeys.DURATION_MS)).isNull();
        assertThat(MDC.get(LoggingContextKeys.USER_ID)).isNull();
    }

    @Test
    @DisplayName("deve omitir header Authorization do MDC")
    void shouldNotLogAuthorizationHeader() throws Exception {
        Mockito.when(userLogResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub").header("Authorization", "Bearer secret-token"))
                .andExpect(status().isOk());
        assertThat(filter.captured.get("Authorization")).isNull();
        assertThat(MDC.get("Authorization")).isNull();
    }

    @RestController
    static class StubController {
        @GetMapping("/stub")
        ResponseEntity<Void> stub() {
            return ResponseEntity.ok().build();
        }
    }
}