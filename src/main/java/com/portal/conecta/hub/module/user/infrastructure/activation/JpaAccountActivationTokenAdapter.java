package com.portal.conecta.hub.module.user.infrastructure.activation;

import com.portal.conecta.hub.module.user.domain.model.AccountActivationToken;
import com.portal.conecta.hub.module.user.domain.model.UserActivationTokenEntity;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationTokenPort;
import com.portal.conecta.hub.module.user.domain.port.UserActivationTokenRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * JPA adapter that creates, hashes, persists and consumes account activation tokens.
 */
@Component
public class JpaAccountActivationTokenAdapter implements AccountActivationTokenPort {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final UserActivationTokenRepository repository;

    public JpaAccountActivationTokenAdapter(UserActivationTokenRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a cryptographically random token and stores only its hash.
     *
     * @param user user associated with the token
     * @param expiresAt expiration instant for the token
     * @return raw activation token to be delivered to the user
     */
    @Override
    public String createToken(UserEntity user, Instant expiresAt) {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        repository.save(new UserActivationTokenEntity(user, hash(rawToken), expiresAt));

        return rawToken;
    }

    /**
     * Finds activation token metadata by hashing the supplied raw token.
     *
     * @param rawToken raw token received from the activation request
     * @return matching token metadata when present
     */
    @Override
    public Optional<AccountActivationToken> findByRawToken(String rawToken) {
        return repository.findByTokenHash(hash(rawToken))
                .map(UserActivationTokenEntity::toDomainToken);
    }

    /**
     * Marks the token matching the supplied raw value as used.
     *
     * @param rawToken raw token received from the activation request
     */
    @Override
    public void markAsUsed(String rawToken) {
        repository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            token.markAsUsed();
            repository.save(token);
        });
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel para token de ativacao.", exception);
        }
    }
}
