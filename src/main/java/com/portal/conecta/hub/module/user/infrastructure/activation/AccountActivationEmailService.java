package com.portal.conecta.hub.module.user.infrastructure.activation;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Service responsible for building and sending account activation e-mails.
 *
 * <p>When no {@link JavaMailSender} is configured, the service logs the missing
 * dependency and lets user creation continue without sending the message.</p>
 */
@Service
@Slf4j
public class AccountActivationEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final AccountActivationProperties properties;

    public AccountActivationEmailService(ObjectProvider<JavaMailSender> mailSenderProvider, AccountActivationProperties properties) {
        this.mailSenderProvider = mailSenderProvider;
        this.properties = properties;
    }

    /**
     * Sends an HTML activation e-mail to a newly created user.
     *
     * @param name recipient name used in the greeting
     * @param email recipient e-mail address
     * @param rawToken raw activation token to be embedded in the activation link
     * @param expiresAt expiration instant shown in the message
     */
    public void sendActivationEmail(String name, String email, String rawToken, Instant expiresAt) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("JavaMailSender nao configurado. E-mail de ativacao nao enviado.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(properties.getMailFrom());
            helper.setTo(email);
            helper.setSubject("Ative sua conta no Portal Conecta");
            helper.setText(buildHtml(name, buildActivationLink(rawToken), expiresAt), true);

            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Nao foi possivel montar o e-mail de ativacao.", exception);
        }
    }

    private String buildActivationLink(String rawToken) {
        return UriComponentsBuilder
                .fromUriString(properties.getBaseUrl())
                .queryParam("token", rawToken)
                .build()
                .toUriString();
    }

    private String buildHtml(String name, String activationLink, Instant expiresAt) {
        return """
                <p>Ola, %s.</p>
                <p>Sua conta no Portal Conecta foi criada.</p>
                <p>Para ativar a conta e definir sua senha, acesse o link abaixo:</p>
                <p><a href="%s">Ativar conta</a></p>
                <p>Este link expira em 24 horas. Validade: %s.</p>
                <p>Se voce nao reconhece esta solicitacao, ignore este e-mail.</p>
                """.formatted(name, activationLink, expiresAt);
    }
}
