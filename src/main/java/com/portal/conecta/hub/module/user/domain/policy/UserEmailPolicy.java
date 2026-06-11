package com.portal.conecta.hub.module.user.domain.policy;

import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class UserEmailPolicy {

    private static final String SENAI_STUDENT_EMAIL_DOMAIN = "@estudante.sesisenai.org.br";
    private static final String WEG_EMAIL_DOMAIN = "@weg.net";

    private static final Set<String> ALLOWED_EMAIL_DOMAINS = Set.of(
            SENAI_STUDENT_EMAIL_DOMAIN,
            WEG_EMAIL_DOMAIN
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final UserRepository userRepository;

    public UserEmailPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String validateForCreation(String email) {
        String normalizedEmail = normalize(email);
        validateInstitutionalDomain(normalizedEmail);
        validateAvailability(normalizedEmail);

        return normalizedEmail;
    }

    private String normalize(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidUserDataException("email é obrigatório.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new InvalidUserDataException("email deve ser válido.");
        }

        return normalizedEmail;
    }

    private void validateInstitutionalDomain(String email) {
        boolean hasAllowedDomain = ALLOWED_EMAIL_DOMAINS.stream().anyMatch(email::endsWith);

        if (!hasAllowedDomain) {
            throw new InvalidUserDataException(
                    "O e-mail deve pertencer aos domínios SENAI ou WEG: "
                            + SENAI_STUDENT_EMAIL_DOMAIN
                            + " ou "
                            + WEG_EMAIL_DOMAIN
            );
        }
    }

    private void validateAvailability(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyInUseException(email);
        }
    }
}
