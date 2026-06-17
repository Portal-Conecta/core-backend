package com.portal.conecta.hub.module.user.domain.policy;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class UserEmailPolicy {

    private static final String SENAI_STUDENT_EMAIL_DOMAIN = "@estudante.sesisenai.org.br";
    private static final String SENAI_TEACHER_EMAIL_DOMAIN = "@edu.sc.senai.br";
    private static final String SENAI_STAFF_EMAIL_DOMAIN = "@sc.senai.br";
    private static final String WEG_EMAIL_DOMAIN = "@weg.net";

    private static final Map<TypeUser, String> DOMAIN_BY_TYPE = Map.of(
            TypeUser.STUDENT, SENAI_STUDENT_EMAIL_DOMAIN,
            TypeUser.REPRESENTATIVE, SENAI_STUDENT_EMAIL_DOMAIN,
            TypeUser.TEACHER, SENAI_TEACHER_EMAIL_DOMAIN,
            TypeUser.SENAI, SENAI_STAFF_EMAIL_DOMAIN,
            TypeUser.WEG, WEG_EMAIL_DOMAIN
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    public String validateForCreation(String email, TypeUser typeUser) {
        String normalizedEmail = normalize(email);
        validateDomainForType(normalizedEmail, typeUser);

        return normalizedEmail;
    }

    public String validateForUpdate(String email, TypeUser typeUser) {
        String normalizedEmail = normalize(email);
        validateDomainForType(normalizedEmail, typeUser);

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

    private void validateDomainForType(String email, TypeUser typeUser) {
        String requiredDomain = DOMAIN_BY_TYPE.get(typeUser);

        if (requiredDomain == null) {
            return;
        }
        if (!email.endsWith(requiredDomain)) {
            throw new InvalidUserDataException("O email deve pertencer ao domínio: " + requiredDomain + " para este usuário.");
        }
    }
}