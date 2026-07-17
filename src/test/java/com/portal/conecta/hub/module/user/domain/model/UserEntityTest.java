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
import java.util.stream.Stream;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserEntityTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createBuildsActiveUserWithPasswordHashAndAuditCreator() {
        UserEntity creator = new UserEntity("Admin", "admin@weg.net", "admin-hash", TypeUser.ADMIN);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

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

    @Test
    void pendingActivationStartsInactiveAndActivationReplacesPassword() {
        UserEntity updater = new UserEntity("Admin", "admin@test.local", "hash", TypeUser.ADMIN);
        UserEntity user = UserEntity.createPendingActivation("Student", "student@test.local", "unusable", TypeUser.STUDENT, updater);
        assertFalse(user.isActive());
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");
        user.activate("new-secret", updater, passwordEncoder);
        assertTrue(user.isActive());
        assertEquals("new-hash", user.getPasswordHash());
        assertSame(updater, user.getUpdatedBy());
    }

    @Test
    void createAndActivateRequirePasswordEncoderAndValidActivationPassword() {
        assertThrows(NullPointerException.class,
                () -> UserEntity.create("User", "user@test.local", "secret", TypeUser.STUDENT, null, null));
        UserEntity user = UserEntity.createPendingActivation("User", "user@test.local", "unusable", TypeUser.STUDENT, null);
        assertThrows(NullPointerException.class, () -> user.activate("secret", null, null));
        assertThrows(InvalidUserDataException.class, () -> user.activate("  ", null, passwordEncoder));
        verifyNoInteractions(passwordEncoder);
    }

    @ParameterizedTest
    @MethodSource("invalidPendingActivationArguments")
    void pendingActivationRejectsInvalidRequiredData(String name, String email, String hash, TypeUser type) {
        assertThrows(InvalidUserDataException.class,
                () -> UserEntity.createPendingActivation(name, email, hash, type, null));
    }

    @Test
    void removedUserCannotBeActivatedOrUpdated() {
        UserEntity user = new UserEntity("Student", "student@test.local", "hash", TypeUser.STUDENT);
        user.delete(null);
        assertThrows(InvalidUserDataException.class, () -> user.activate("secret", null, passwordEncoder));
        assertThrows(InvalidUserDataException.class, () -> user.update("Other", null));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateChangesOnlyMeaningfullyDifferentName() {
        UserEntity updater = new UserEntity("Admin", "admin@test.local", "hash", TypeUser.ADMIN);
        UserEntity user = new UserEntity("Student", "student@test.local", "hash", TypeUser.STUDENT);
        assertEquals(java.util.List.of("name"), user.update(" Other ", updater));
        assertEquals("Other", user.getName());
        assertEquals("student@test.local", user.getEmail());
        assertNull(user.getAvatarUrl());
        assertTrue(user.update(null, updater).isEmpty());
        assertTrue(user.update("   ", updater).isEmpty());
    }

    @Test
    void promotionAndDemotionUpdateTypeAndAudit() {
        UserEntity executor = new UserEntity("Admin", "admin@test.local", "hash", TypeUser.ADMIN);
        UserEntity user = new UserEntity("Student", "student@test.local", "hash", TypeUser.STUDENT);
        user.promoteTo(TypeUser.REPRESENTATIVE, executor);
        assertEquals(TypeUser.REPRESENTATIVE, user.getTypeUser());
        assertSame(executor, user.getUpdatedBy());
        user.demoteTo(TypeUser.STUDENT, executor);
        assertEquals(TypeUser.STUDENT, user.getTypeUser());
        assertSame(executor, user.getUpdatedBy());
    }

    @Test
    void equalityUsesPersistentIdentity() {
        UserEntity first = new UserEntity("First", "first@test.local", "hash", TypeUser.STUDENT);
        UserEntity same = new UserEntity("Same", "same@test.local", "hash", TypeUser.STUDENT);
        UserEntity other = new UserEntity("Other", "other@test.local", "hash", TypeUser.STUDENT);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(first, "id", id);
        ReflectionTestUtils.setField(same, "id", id);
        ReflectionTestUtils.setField(other, "id", UUID.randomUUID());
        assertEquals(first, first);
        assertEquals(first, same);
        org.junit.jupiter.api.Assertions.assertNotEquals(first, other);
        org.junit.jupiter.api.Assertions.assertNotEquals(first, null);
        org.junit.jupiter.api.Assertions.assertNotEquals(first, "user");
        org.junit.jupiter.api.Assertions.assertNotEquals(new UserEntity("New", "new@test.local", "hash", TypeUser.STUDENT), same);
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

    private static Stream<Arguments> invalidPendingActivationArguments() {
        return Stream.of(
                Arguments.of(null, "user@test.local", "hash", TypeUser.STUDENT),
                Arguments.of("   ", "user@test.local", "hash", TypeUser.STUDENT),
                Arguments.of("User", null, "hash", TypeUser.STUDENT),
                Arguments.of("User", "   ", "hash", TypeUser.STUDENT),
                Arguments.of("User", "user@test.local", null, TypeUser.STUDENT),
                Arguments.of("User", "user@test.local", "   ", TypeUser.STUDENT),
                Arguments.of("User", "user@test.local", "hash", null)
        );
    }
}
