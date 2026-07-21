package com.portal.conecta.hub.module.user.infrastructure.activation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties used to build account activation e-mails.
 */
@Component
@ConfigurationProperties(prefix = "app.account-activation")
public class AccountActivationProperties {

    private String baseUrl = "http://localhost:3002/ativar-conta";
    private String mailFrom = "no-reply@portal-conecta.local";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }
}
