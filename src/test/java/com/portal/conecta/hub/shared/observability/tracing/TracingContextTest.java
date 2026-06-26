package com.portal.conecta.hub.shared.observability.tracing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "management.tracing.sampling.probability=1.0",
        "management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:4318/v1/traces"
})
class TracingContextTest {

    @Test
    void applicationShouldStartWithTracingConfigured() {
    }
}