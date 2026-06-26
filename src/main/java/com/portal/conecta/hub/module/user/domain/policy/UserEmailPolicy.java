package com.portal.conecta.hub.module.user.domain.policy;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Política de validação e normalização de e-mail por tipo de usuário.
 *
 * <p>Define os domínios obrigatórios por tipo:
 * <ul>
 *   <li>{@code STUDENT} e {@code REPRESENTATIVE} — {@code @estudante.sesisenai.org.br}</li>
 *   <li>{@code TEACHER} — {@code @edu.sc.senai.br}</li>
 *   <li>{@code SENAI} — {@code @sc.senai.br}</li>
 *   <li>{@code WEG} — {@code @weg.net}</li>
 *   <li>{@code ADMIN} — sem restrição de domínio.</li>
 * </ul>
 *
 * <p>Toda validação normaliza o e-mail para minúsculas antes de verificar formato e domínio.
 */
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

    /**
     * Valida e normaliza o e-mail para criação de usuário.
     *
     * @param email    e-mail informado; não pode ser nulo ou em branco.
     * @param typeUser tipo do usuário que determina o domínio obrigatório.
     * @return e-mail normalizado (trim + lowercase).
     * @throws InvalidUserDataException se o e-mail for inválido ou não pertencer ao domínio esperado.
     */
    public String validateForCreation(String email, TypeUser typeUser) {
        String normalizedEmail = normalize(email);
        validateDomainForType(normalizedEmail, typeUser);

        return normalizedEmail;
    }

    /**
     * Valida e normaliza o e-mail para atualização de usuário.
     * Aplica as mesmas regras de {@link #validateForCreation}.
     *
     * @param email    novo e-mail informado.
     * @param typeUser tipo atual do usuário.
     * @return e-mail normalizado (trim + lowercase).
     * @throws InvalidUserDataException se o e-mail for inválido ou não pertencer ao domínio esperado.
     */
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