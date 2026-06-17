package com.portal.conecta.hub.module.user.application.use_case;


import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserEmailPolicyTest {

    private UserEmailPolicy userEmailPolicy;

    @BeforeEach
    void setUp() {
        userEmailPolicy = new UserEmailPolicy();
    }

    @ParameterizedTest
    @CsvSource({
            "' STUDENT@ESTUDANTE.SESISENAI.ORG.BR ', student@estudante.sesisenai.org.br, STUDENT",
            "employee@weg.net, employee@weg.net, WEG",
            "REPRESENTATIVE@ESTUDANTE.SESISENAI.ORG.BR, representative@estudante.sesisenai.org.br, REPRESENTATIVE",
            "teacher@edu.sc.senai.br, teacher@edu.sc.senai.br, TEACHER",
            "staff@sc.senai.br, staff@sc.senai.br, SENAI",
            "qualquer@dominio.com, qualquer@dominio.com, ADMIN"
    })
    void validateForCreationNormalizesAndAcceptsCompatibleEmail(String email, String expectedEmail, TypeUser typeUser) {
        String normalizedEmail = userEmailPolicy.validateForCreation(email, typeUser);

        assertEquals(expectedEmail, normalizedEmail);
    }

    @ParameterizedTest
    @CsvSource({
            "' STUDENT@ESTUDANTE.SESISENAI.ORG.BR ', student@estudante.sesisenai.org.br, STUDENT",
            "employee@weg.net, employee@weg.net, WEG",
            "teacher@edu.sc.senai.br, teacher@edu.sc.senai.br, TEACHER",
            "staff@sc.senai.br, staff@sc.senai.br, SENAI",
            "qualquer@dominio.com, qualquer@dominio.com, ADMIN"
    })
    void validateForUpdateNormalizesAndAcceptsCompatibleEmail(String email, String expectedEmail, TypeUser typeUser) {
        String normalizedEmail = userEmailPolicy.validateForUpdate(email, typeUser);

        assertEquals(expectedEmail, normalizedEmail);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void validateForCreationRejectsMissingEmail(String email) {
        assertThrows(InvalidUserDataException.class,
                () -> userEmailPolicy.validateForCreation(email, TypeUser.STUDENT));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void validateForUpdateRejectsMissingEmail(String email) {
        assertThrows(InvalidUserDataException.class,
                () -> userEmailPolicy.validateForUpdate(email, TypeUser.STUDENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "student",
            "student@weg",
            "@weg.net"
    })
    void validateForCreationRejectsInvalidEmailFormat(String email) {
        assertThrows(InvalidUserDataException.class,
                () -> userEmailPolicy.validateForCreation(email, TypeUser.STUDENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "student",
            "student@weg",
            "@weg.net"
    })
    void validateForUpdateRejectsInvalidEmailFormat(String email) {
        assertThrows(InvalidUserDataException.class,
                () -> userEmailPolicy.validateForUpdate(email, TypeUser.STUDENT));
    }

    @ParameterizedTest
    @CsvSource({
            "student@weg.net, STUDENT",
            "representative@weg.net, REPRESENTATIVE",
            "teacher@weg.net, TEACHER",
            "staff@weg.net, SENAI",
            "employee@estudante.sesisenai.org.br, WEG"
    })
    void validateForCreationRejectsEmailIncompatibleWithType(String email, TypeUser typeUser) {
        assertThrows(InvalidUserDataException.class,
                () -> userEmailPolicy.validateForCreation(email, typeUser));
    }

    @ParameterizedTest
    @CsvSource({
            "student@weg.net, STUDENT",
            "representative@weg.net, REPRESENTATIVE",
            "teacher@weg.net, TEACHER",
            "staff@weg.net, SENAI",
            "employee@estudante.sesisenai.org.br, WEG"
    })
    void validateForUpdateRejectsEmailIncompatibleWithType(String email, TypeUser typeUser) {
        assertThrows(InvalidUserDataException.class,
                () -> userEmailPolicy.validateForUpdate(email, typeUser));
    }
}