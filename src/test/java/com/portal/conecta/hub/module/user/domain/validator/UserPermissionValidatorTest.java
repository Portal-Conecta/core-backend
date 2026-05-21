package com.portal.conecta.hub.module.user.domain.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class UserPermissionValidatorTest {

    private final UserPermissionValidator userPermissionValidator = new UserPermissionValidator();

    @ParameterizedTest
    @MethodSource("allowedCreations")
    void canCreateAllowsExpectedCombinations(TypeUser authenticatedType, TypeUser targetType) {
        assertTrue(userPermissionValidator.canCreate(authenticatedType, targetType));
        assertDoesNotThrow(() -> userPermissionValidator.validateCanCreate(authenticatedType, targetType));
    }

    @ParameterizedTest
    @MethodSource("blockedCreations")
    void canCreateBlocksExpectedCombinations(TypeUser authenticatedType, TypeUser targetType) {
        assertFalse(userPermissionValidator.canCreate(authenticatedType, targetType));
        assertThrows(
                UserPermissionDeniedException.class,
                () -> userPermissionValidator.validateCanCreate(authenticatedType, targetType)
        );
    }

    @Test
    void canCreateReturnsFalseWhenAnyTypeIsMissing() {
        assertFalse(userPermissionValidator.canCreate(null, TypeUser.STUDENT));
        assertFalse(userPermissionValidator.canCreate(TypeUser.ADMIN, null));
    }

    @Test
    void validateCanCreateRejectsMissingTargetTypeAsInvalidData() {
        assertThrows(
                InvalidUserDataException.class,
                () -> userPermissionValidator.validateCanCreate(TypeUser.ADMIN, null)
        );
    }

    @ParameterizedTest
    @MethodSource("allowedDeactivations")
    void canDeactivateAllowsExpectedCombinations(TypeUser authenticatedType, TypeUser targetType) {
        assertTrue(userPermissionValidator.canDeactivate(authenticatedType, targetType));
        assertDoesNotThrow(() -> userPermissionValidator.validateCanDeactivate(authenticatedType, targetType));
    }

    @ParameterizedTest
    @MethodSource("blockedDeactivations")
    void canDeactivateBlocksExpectedCombinations(TypeUser authenticatedType, TypeUser targetType) {
        assertFalse(userPermissionValidator.canDeactivate(authenticatedType, targetType));
        assertThrows(
                UserPermissionDeniedException.class,
                () -> userPermissionValidator.validateCanDeactivate(authenticatedType, targetType)
        );
    }

    @Test
    void canDeactivateReturnsFalseWhenAnyTypeIsMissing() {
        assertFalse(userPermissionValidator.canDeactivate(null, TypeUser.STUDENT));
        assertFalse(userPermissionValidator.canDeactivate(TypeUser.ADMIN, null));
    }

    @Test
    void validateCanDeactivateRejectsMissingTargetTypeAsInvalidData() {
        assertThrows(
                InvalidUserDataException.class,
                () -> userPermissionValidator.validateCanDeactivate(TypeUser.ADMIN, null)
        );
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"ADMIN", "SENAI", "WEG"})
    void canListUsersAllowsAdministrativeProfiles(TypeUser type) {
        assertTrue(userPermissionValidator.canListUsers(type));
        assertDoesNotThrow(() -> userPermissionValidator.validateCanListUsers(type));
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"STUDENT", "TEACHER", "REPRESENTATIVE"})
    void canListUsersBlocksCommonProfiles(TypeUser type) {
        assertFalse(userPermissionValidator.canListUsers(type));
        assertThrows(
                UserPermissionDeniedException.class,
                () -> userPermissionValidator.validateCanListUsers(type)
        );
    }

    @Test
    void canListUsersReturnsFalseWhenTypeIsMissing() {
        assertFalse(userPermissionValidator.canListUsers(null));
    }

    @Test
    void validateCanListUsersRejectsMissingTypeAsPermissionDenied() {
        assertThrows(
                UserPermissionDeniedException.class,
                () -> userPermissionValidator.validateCanListUsers(null)
        );
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> allowedCreations() {
        Stream<org.junit.jupiter.params.provider.Arguments> adminAllowedCreations = Arrays.stream(TypeUser.values())
                .map(targetType -> arguments(TypeUser.ADMIN, targetType));

        Stream<org.junit.jupiter.params.provider.Arguments> scopedAllowedCreations = Stream.of(
                arguments(TypeUser.SENAI, TypeUser.STUDENT),
                arguments(TypeUser.SENAI, TypeUser.TEACHER),
                arguments(TypeUser.WEG, TypeUser.STUDENT),
                arguments(TypeUser.WEG, TypeUser.WEG)
        );

        return Stream.concat(adminAllowedCreations, scopedAllowedCreations);
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> blockedCreations() {
        return Stream.of(
                arguments(TypeUser.SENAI, TypeUser.ADMIN),
                arguments(TypeUser.SENAI, TypeUser.WEG),
                arguments(TypeUser.WEG, TypeUser.TEACHER),
                arguments(TypeUser.WEG, TypeUser.ADMIN),
                arguments(TypeUser.TEACHER, TypeUser.STUDENT),
                arguments(TypeUser.STUDENT, TypeUser.STUDENT),
                arguments(TypeUser.REPRESENTATIVE, TypeUser.STUDENT)
        );
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> allowedDeactivations() {
        Stream<org.junit.jupiter.params.provider.Arguments> adminAllowedDeactivations = Arrays.stream(TypeUser.values())
                .map(targetType -> arguments(TypeUser.ADMIN, targetType));

        Stream<org.junit.jupiter.params.provider.Arguments> scopedAllowedDeactivations = Stream.of(
                arguments(TypeUser.SENAI, TypeUser.STUDENT),
                arguments(TypeUser.SENAI, TypeUser.TEACHER),
                arguments(TypeUser.SENAI, TypeUser.REPRESENTATIVE),
                arguments(TypeUser.WEG, TypeUser.STUDENT),
                arguments(TypeUser.WEG, TypeUser.REPRESENTATIVE)
        );

        return Stream.concat(adminAllowedDeactivations, scopedAllowedDeactivations);
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> blockedDeactivations() {
        return Stream.of(
                arguments(TypeUser.SENAI, TypeUser.ADMIN),
                arguments(TypeUser.SENAI, TypeUser.WEG),
                arguments(TypeUser.WEG, TypeUser.TEACHER),
                arguments(TypeUser.WEG, TypeUser.ADMIN),
                arguments(TypeUser.TEACHER, TypeUser.STUDENT),
                arguments(TypeUser.STUDENT, TypeUser.STUDENT),
                arguments(TypeUser.REPRESENTATIVE, TypeUser.STUDENT)
        );
    }
}