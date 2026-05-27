package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassMembershipValidatorTest {

    private ClassMembershipValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClassMembershipValidator();
    }

    // --- validateExecutorType ---

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"ADMIN", "SENAI"})
    @DisplayName("não deve lançar exceção quando executor é ADMIN ou SENAI")
    void shouldNotThrowForAllowedExecutors(TypeUser type) {
        assertThatCode(() -> validator.validateExecutorType(type))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"WEG", "STUDENT", "TEACHER", "REPRESENTATIVE"})
    @DisplayName("deve lançar UserPermissionDeniedException para executores não autorizados")
    void shouldThrowForDisallowedExecutors(TypeUser type) {
        assertThatThrownBy(() -> validator.validateExecutorType(type))
                .isInstanceOf(UserPermissionDeniedException.class);
    }

    // --- validateNoSelfAssociation ---

    @Test
    @DisplayName("deve lançar ClassMembershipException quando usuário tenta associar a si mesmo")
    void shouldThrowOnSelfAssociation() {
        UUID sameId = UUID.randomUUID();
        assertThatThrownBy(() -> validator.validateNoSelfAssociation(sameId, sameId))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("não deve lançar exceção quando IDs são diferentes")
    void shouldNotThrowWhenDifferentUsers() {
        assertThatCode(() -> validator.validateNoSelfAssociation(UUID.randomUUID(), UUID.randomUUID()))
                .doesNotThrowAnyException();
    }

    // --- validateClassRoleNotRepresentative ---

    @Test
    @DisplayName("deve lançar ClassMembershipException quando role é REPRESENTATIVE")
    void shouldThrowWhenRoleIsRepresentative() {
        assertThatThrownBy(() -> validator.validateClassRoleNotRepresentative(ClassRole.REPRESENTATIVE))
                .isInstanceOf(ClassMembershipException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ClassRole.class, names = {"STUDENT", "TEACHER"})
    @DisplayName("não deve lançar exceção para roles permitidos")
    void shouldNotThrowForAllowedRoles(ClassRole role) {
        assertThatCode(() -> validator.validateClassRoleNotRepresentative(role))
                .doesNotThrowAnyException();
    }

    // --- validateTargetUserType ---

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"STUDENT", "TEACHER"})
    @DisplayName("não deve lançar exceção para tipos de usuário permitidos como alvo")
    void shouldNotThrowForAllowedTargetTypes(TypeUser type) {
        assertThatCode(() -> validator.validateTargetUserType(type))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"ADMIN", "SENAI", "WEG", "REPRESENTATIVE"})
    @DisplayName("deve lançar ClassMembershipException para tipos não associáveis")
    void shouldThrowForDisallowedTargetTypes(TypeUser type) {
        assertThatThrownBy(() -> validator.validateTargetUserType(type))
                .isInstanceOf(ClassMembershipException.class);
    }

    // --- validateTypeAndRoleCombination ---

    @Test
    @DisplayName("não deve lançar exceção para STUDENT com role STUDENT")
    void shouldNotThrowForStudentWithStudentRole() {
        assertThatCode(() -> validator.validateTypeAndRoleCombination(TypeUser.STUDENT, ClassRole.STUDENT))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("não deve lançar exceção para TEACHER com role TEACHER")
    void shouldNotThrowForTeacherWithTeacherRole() {
        assertThatCode(() -> validator.validateTypeAndRoleCombination(TypeUser.TEACHER, ClassRole.TEACHER))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException para STUDENT com role TEACHER")
    void shouldThrowForStudentWithTeacherRole() {
        assertThatThrownBy(() -> validator.validateTypeAndRoleCombination(TypeUser.STUDENT, ClassRole.TEACHER))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException para TEACHER com role STUDENT")
    void shouldThrowForTeacherWithStudentRole() {
        assertThatThrownBy(() -> validator.validateTypeAndRoleCombination(TypeUser.TEACHER, ClassRole.STUDENT))
                .isInstanceOf(ClassMembershipException.class);
    }

    // --- validateNoDuplicateMembership ---

    @Test
    @DisplayName("deve lançar ClassMembershipException quando vínculo já existe")
    void shouldThrowWhenMembershipAlreadyExists() {
        assertThatThrownBy(() -> validator.validateNoDuplicateMembership(true))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("não deve lançar exceção quando não há vínculo duplicado")
    void shouldNotThrowWhenNoMembershipExists() {
        assertThatCode(() -> validator.validateNoDuplicateMembership(false))
                .doesNotThrowAnyException();
    }

    // --- validateStudentClassLimit ---

    @Test
    @DisplayName("deve lançar ClassMembershipException quando aluno já possui turma ativa")
    void shouldThrowWhenStudentAlreadyHasActiveClass() {
        assertThatThrownBy(() -> validator.validateStudentClassLimit(ClassRole.STUDENT, 1L))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("não deve lançar exceção quando aluno não possui turma ativa")
    void shouldNotThrowWhenStudentHasNoActiveClass() {
        assertThatCode(() -> validator.validateStudentClassLimit(ClassRole.STUDENT, 0L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("não deve lançar exceção para TEACHER com múltiplas turmas ativas")
    void shouldNotThrowForTeacherWithMultipleClasses() {
        assertThatCode(() -> validator.validateStudentClassLimit(ClassRole.TEACHER, 5L))
                .doesNotThrowAnyException();
    }
}
