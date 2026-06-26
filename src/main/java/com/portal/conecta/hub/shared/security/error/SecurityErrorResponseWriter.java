package com.portal.conecta.hub.shared.security.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.portal.conecta.hub.shared.exception.ApiError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Escreve respostas de erro de segurança como JSON no padrão {@link com.portal.conecta.hub.shared.exception.ApiError}.
 *
 * <p>Utilizado pelo {@link com.portal.conecta.hub.shared.security.config.SecurityConfig} e pelo {@link com.portal.conecta.hub.shared.security.filter.JwtAuthenticationFilter}
 * para garantir que erros de autenticação e autorização sigam o mesmo contrato
 * de resposta dos demais erros da API, mesmo fora do ciclo do {@code DispatcherServlet}.
 *
 * <p>Não escreve na resposta se ela já estiver commitada.
 */
@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Serializa um {@link com.portal.conecta.hub.shared.exception.ApiError} e o escreve na resposta HTTP.
     *
     * @param request  requisição de origem, usada para compor o {@code path} do erro.
     * @param response resposta onde o erro será escrito.
     * @param status   status HTTP a ser retornado.
     * @param message  mensagem de erro exibida ao consumidor da API.
     */
    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiError.of(status, message, request.getRequestURI()));
    }
    
}
