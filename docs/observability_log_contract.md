# Contrato de Log Estruturado — Hub Core

## Objetivo

Documentar os campos garantidos na saída de log estruturado do core-backend, compatível com coleta por container (Docker + Grafana Alloy + Loki).

## Formato de saída

Logstash JSON via `logging.structured.format.console: logstash` (Spring Boot 4.x nativo).

## Exemplo de log completo

```json
{
  "@timestamp": "2026-06-23T10:00:00.000Z",
  "level": "INFO",
  "service": "hub",
  "environment": "dev",
  "correlationId": "4c9a6b6e-9a4f-4e8a-b4bb-ecf20f33b9a1",
  "userId": "8e7a3f9d-2d19-49f6-91f5-1e1a01c38100",
  "method": "POST",
  "path": "/users",
  "status": "201",
  "durationMs": "42",
  "logger": "com.portal.conecta.hub.module.user.application.use_case.CreateUserUseCase",
  "message": "Usuário criado com sucesso. targetUserId=2f18c1a2-9b10-4f1b-9359-bdc16ccbe9c2"
}
```

## Contrato de campos

| Campo          | Origem                        | Sempre presente | Observação                                      |
|----------------|-------------------------------|-----------------|------------------------------------------------|
| `@timestamp`   | Logstash (automático)         | Sim             |                                                |
| `level`        | SLF4J (automático)            | Sim             | INFO, WARN, ERROR, DEBUG                       |
| `service`      | `spring.application.name`     | Sim             | Valor: `hub`                                   |
| `environment`  | `PORTAL_ENVIRONMENT`          | Sim             | Default: `local`                               |
| `logger`       | SLF4J (renomeado de `logger_name`) | Sim        | Nome completo da classe                        |
| `message`      | SLF4J                         | Sim             |                                                |
| `correlationId`| `CorrelationIdFilter` via MDC | Sim             | Preservado do header ou UUID gerado            |
| `userId`       | `AccessLogFilter` via MDC     | Não             | Presente apenas em requisições autenticadas    |
| `method`       | `AccessLogFilter` via MDC     | Não             | Presente apenas no log de acesso HTTP          |
| `path`         | `AccessLogFilter` via MDC     | Não             | Sem query string. Presente apenas no access log|
| `status`       | `AccessLogFilter` via MDC     | Não             | Presente apenas no access log                  |
| `durationMs`   | `AccessLogFilter` via MDC     | Não             | Presente apenas no access log                  |

## Configuração aplicada

```yaml
logging:
  structured:
    format:
      console: logstash
    json:
      add:
        service: ${spring.application.name}
        environment: ${PORTAL_ENVIRONMENT:local}
      rename:
        logger_name: logger
```

Spring Boot 4.0.6 suporta nativamente `logging.structured.format.console: logstash` sem dependências externas.

## Header de rastreamento

O header `X-Correlation-Id` é lido na requisição e devolvido na resposta. Regras de validação:

- Preserva valor válido (aceita `^[A-Za-z0-9._:-]+$`, máximo 128 caracteres).
- Gera UUID quando ausente, vazio ou inválido.

## O que nunca aparece no log

- Body de request ou response
- Query string
- Header `Authorization`
- Cookies
- Tokens JWT
- E-mail do usuário