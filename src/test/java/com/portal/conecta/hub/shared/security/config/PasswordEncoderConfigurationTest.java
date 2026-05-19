package com.portal.conecta.hub.shared.security.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderConfigurationTest {

    private final PasswordEncoderConfiguration configuration = new PasswordEncoderConfiguration();

    @Test
    void passwordEncoderCreatesBcryptEncoder() {
        PasswordEncoder passwordEncoder = configuration.passwordEncoder();
        String encodedPassword = passwordEncoder.encode("secret");

        assertAll(
                () -> assertNotEquals("secret", encodedPassword),
                () -> assertTrue(passwordEncoder.matches("secret", encodedPassword)),
                () -> assertFalse(passwordEncoder.matches("other-secret", encodedPassword))
        );
    }
}
