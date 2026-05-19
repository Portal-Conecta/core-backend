package com.portal.conecta.hub.module.user.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;

import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserEntityTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createBuildsActiveUserWithPasswordHashAndAuditCreator() {
        UserEntity creator = new UserEntity("Admin", "admin@weg.net", "admin-hash", TypeUser.ADMIN);
        when(Objects.requireNonNull(passwordEncoder.encode("secret"))).thenReturn("encoded-secret");

        UserEntity user = UserEntity.create(
                "  Student One  ",
                " student@estudante.sesisenai.org.br ",
                "secret",
                TypeUser.STUDENT,
                creator,
                passwordEncoder
        );

        assertAll(
                () -> assertEquals("Student One", user.getName()),
                () -> assertEquals("student@estudante.sesisenai.org.br", user.getEmail()),
                () -> assertEquals("encoded-secret", user.getPasswordHash()),
                () -> assertEquals(TypeUser.STUDENT, user.getTypeUser()),
                () -> assertSame(creator, user.getCreatedBy()),
                () -> assertSame(creator, user.getUpdatedBy()),
                () -> assertTrue(user.isActive()),
                () -> assertNull(user.getDeletedAt())
        );
        verify(passwordEncoder).encode("secret");
    }

    @ParameterizedTest
    @MethodSource("invalidCreateArguments")
    void createRejectsInvalidRequiredDataWithoutEncodingPassword(
            String name,
            String email,
            String password,
            TypeUser typeUser
    ) {
        assertThrows(
                InvalidUserDataException.class,
                () -> UserEntity.create(name, email, password, typeUser, null, passwordEncoder)
        );

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deleteMarksUserInactiveAndStoresDeletionAudit() {
        UserEntity deletedBy = new UserEntity("Admin", "admin@weg.net", "admin-hash", TypeUser.ADMIN);
        UserEntity user = new UserEntity("Student", "student@weg.net", "student-hash", TypeUser.STUDENT);

        user.delete(deletedBy);

        assertAll(
                () -> assertFalse(user.isActive()),
                () -> assertNotNull(user.getDeletedAt()),
                () -> assertSame(deletedBy, user.getDeletedBy())
        );
    }

    private static Stream<Arguments> invalidCreateArguments() {
        return Stream.of(
                Arguments.of(null, "student@weg.net", "secret", TypeUser.STUDENT),
                Arguments.of("   ", "student@weg.net", "secret", TypeUser.STUDENT),
                Arguments.of("Student", null, "secret", TypeUser.STUDENT),
                Arguments.of("Student", "   ", "secret", TypeUser.STUDENT),
                Arguments.of("Student", "student@weg.net", null, TypeUser.STUDENT),
                Arguments.of("Student", "student@weg.net", "   ", TypeUser.STUDENT),
                Arguments.of("Student", "student@weg.net", "secret", null)
        );
    }
}
