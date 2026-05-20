package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    @DisplayName("deve permitir criação para WEG")
    void shouldAllowWegToCreate() {
        assertThat(validator.canCreate(TypeUser.WEG)).isTrue();
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
}