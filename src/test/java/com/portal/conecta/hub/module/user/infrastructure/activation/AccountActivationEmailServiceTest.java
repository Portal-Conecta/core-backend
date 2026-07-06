package com.portal.conecta.hub.module.user.infrastructure.activation;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountActivationEmailServiceTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    private AccountActivationProperties properties;
    private AccountActivationEmailService service;

    @BeforeEach
    void setUp() {
        properties = new AccountActivationProperties();
        properties.setBaseUrl("https://portal-conecta.example.com/ativar-conta");
        properties.setMailFrom("no-reply@portal-conecta.local");

        service = new AccountActivationEmailService(mailSenderProvider, properties);
    }

    @Test
    void deveEnviarEmailComConteudoCorreto() throws Exception {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        jakarta.mail.Session session = jakarta.mail.Session.getDefaultInstance(new java.util.Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendActivationEmail(
                "Maria Silva",
                "maria.silva@example.com",
                "raw-token-123",
                Instant.parse("2026-07-03T10:00:00Z")
        );

        verify(mailSender).send(mimeMessage);

        assertThat(mimeMessage.getFrom()[0].toString()).contains(properties.getMailFrom());
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("maria.silva@example.com");
        assertThat(mimeMessage.getSubject()).isEqualTo("Ative sua conta no Portal Conecta");

        String corpo = extractHtmlBody(mimeMessage);
        assertThat(corpo).contains("Maria Silva");
        assertThat(corpo).contains(properties.getBaseUrl());
        assertThat(corpo).contains("token=raw-token-123");
    }

    @Test
    void naoDeveLancarExcecaoQuandoMailSenderNaoConfigurado() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertThatCode(() -> service.sendActivationEmail(
                "Maria Silva", "maria.silva@example.com", "raw-token-123", Instant.now()
        )).doesNotThrowAnyException();

        verifyNoInteractions(mailSender);
    }

    @Test
    void devePropagarFalhaDeEnvioDoSmtp() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        jakarta.mail.Session session = jakarta.mail.Session.getDefaultInstance(new java.util.Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new MailSendException("Falha de conexao SMTP"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendActivationEmail(
                "Maria Silva", "maria.silva@example.com", "raw-token-123", Instant.now()
        )).isInstanceOf(MailSendException.class);
    }

    /**
     * Extrai o corpo HTML ja decodificado (sem escapes de quoted-printable),
     * navegando pela estrutura multipart/mixed -> multipart/related do e-mail.
     */
    private String extractHtmlBody(MimeMessage mimeMessage) throws Exception {
        mimeMessage.saveChanges();
        Object content = mimeMessage.getContent();
        if (content instanceof Multipart multipart) {
            return extractFromMultipart(multipart);
        }
        return content.toString();
    }

    private String extractFromMultipart(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            Object partContent = part.getContent();
            if (partContent instanceof Multipart nested) {
                String result = extractFromMultipart(nested);
                if (result != null) {
                    return result;
                }
            } else if (part.isMimeType("text/html")) {
                return (String) partContent;
            }
        }
        return null;
    }
}