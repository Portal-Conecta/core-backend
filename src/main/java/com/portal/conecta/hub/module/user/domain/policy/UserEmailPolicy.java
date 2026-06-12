package com.portal.conecta.hub.module.user.domain.policy;

import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    private final UserRepository userRepository;

    public UserEmailPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String validateForCreation(String email, TypeUser typeUser) {
        String normalizedEmail = normalize(email);
        validateDomainForType(normalizedEmail, typeUser);
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

    private void validateAvailability(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyInUseException(email);
        }
    }

    private void validateDomainForType (String email, TypeUser typeUser){
        String requiredDomain = DOMAIN_BY_TYPE.get(typeUser);

        if (requiredDomain == null){
            return;
        }
        if (!email.endsWith(requiredDomain)){
            throw new InvalidUserDataException("O email deve pertencer ao domínio: "+requiredDomain+ " para este usuário.");
        }
    }

    private void validateAvaibilityForUpdate(String email, UUID currentUserId){
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId)){
            throw new EmailAlreadyInUseException(email);
        }
    }

}
