package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassPermissionValidatorTest {

    private ClassPermissionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClassPermissionValidator();
    }

    @Test
    @DisplayName("deve permitir criação para ADMIN")
    void shouldAllowAdminToCreate() {
        assertThat(validator.canCreate(TypeUser.ADMIN)).isTrue();
    }

    @Test
    @DisplayName("deve permitir criação para SENAI")
    void shouldAllowSenaiToCreate() {
        assertThat(validator.canCreate(TypeUser.SENAI)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"STUDENT", "REPRESENTATIVE", "TEACHER"})
    @DisplayName("deve negar criação para perfis não autorizados")
    void shouldDenyUnauthorizedProfiles(TypeUser type) {
        assertThat(validator.canCreate(type)).isFalse();
    }

    @Test
    @DisplayName("deve negar criação para STUDENT")
    void shouldDenyStudent() {
        assertThat(validator.canCreate(TypeUser.STUDENT)).isFalse();
    }

    @Test
    @DisplayName("deve negar criação para REPRESENTATIVE")
    void shouldDenyRepresentative() {
        assertThat(validator.canCreate(TypeUser.REPRESENTATIVE)).isFalse();
    }

    @Test
    @DisplayName("deve negar criação para TEACHER")
    void shouldDenyTeacher() {
        assertThat(validator.canCreate(TypeUser.TEACHER)).isFalse();
    }

    @Test
    @DisplayName("deve retornar false quando type é nulo")
    void shouldReturnFalseWhenTypeIsNull() {
        assertThat(validator.canCreate(null)).isFalse();
    }

    @Test
    @DisplayName("validateCanDeactivate não deve lançar exceção para ADMIN")
    void shouldNotThrowWhenAdminDeactivates() {
        assertThatCode(() -> validator.validateCanDeactivate(TypeUser.ADMIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCanDeactivate não deve lançar exceção para SENAI")
    void shouldNotThrowWhenSenaiDeactivates() {
        assertThatCode(() -> validator.validateCanDeactivate(TypeUser.SENAI))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"STUDENT", "REPRESENTATIVE", "TEACHER"})
    @DisplayName("validateCanDeactivate deve lançar exceção para perfis não autorizados")
    void shouldThrowWhenUnauthorizedDeactivates(TypeUser type) {
        assertThatThrownBy(() -> validator.validateCanDeactivate(type))
                .isInstanceOf(UserPermissionDeniedException.class);
    }

    @Test
    @DisplayName("validateCanDeactivate deve lançar exceção quando type é nulo")
    void shouldThrowWhenNullDeactivates() {
        assertThatThrownBy(() -> validator.validateCanDeactivate(null))
                .isInstanceOf(UserPermissionDeniedException.class);
    }

    @Test
    @DisplayName("validateCanReactivate não deve lançar exceção para ADMIN")
    void shouldNotThrowWhenAdminReactivates() {
        assertThatCode(() -> validator.validateCanReactivate(TypeUser.ADMIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCanReactivate não deve lançar exceção para SENAI")
    void shouldNotThrowWhenSenaiReactivates() {
        assertThatCode(() -> validator.validateCanReactivate(TypeUser.SENAI))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"STUDENT", "REPRESENTATIVE", "TEACHER"})
    @DisplayName("validateCanReactivate deve lançar exceção para perfis não autorizados")
    void shouldThrowWhenUnauthorizedReactivates(TypeUser type) {
        assertThatThrownBy(() -> validator.validateCanReactivate(type))
                .isInstanceOf(UserPermissionDeniedException.class);
    }

    @Test
    @DisplayName("validateCanReactivate deve lançar exceção quando type é nulo")
    void shouldThrowWhenNullReactivates() {
        assertThatThrownBy(() -> validator.validateCanReactivate(null))
                .isInstanceOf(UserPermissionDeniedException.class);
    }
}