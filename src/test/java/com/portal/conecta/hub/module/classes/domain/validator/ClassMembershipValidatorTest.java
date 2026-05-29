package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClassMembershipValidatorTest {


    private ClassMembershipValidator validator;

    private UserEntity executor;
    private UserEntity targetStudent;
    private UserEntity targetTeacher;
    private ClassEntity activeClass;

    @BeforeEach
    void setUp() {
        validator = new ClassMembershipValidator();
        executor = new UserEntity("Executor", "executor@test.com", "hash", TypeUser.SENAI);
        targetStudent = new UserEntity("Student", "student@test.com", "hash", TypeUser.STUDENT);
        targetTeacher = new UserEntity("Teacher", "teacher@test.com", "hash", TypeUser.TEACHER);
        CourseEntity course = new CourseEntity("Curso", "CRS");
        activeClass = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);
    }

    // --- validateExecutorCanAddMember ---

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"ADMIN", "SENAI"})
    @DisplayName("não deve lançar exceção quando executor é ADMIN ou SENAI")
    void shouldNotThrowForAllowedExecutors(TypeUser type) {
        UUID executorId = UUID.randomUUID();
        assertThatCode(() -> validator.validateExecutorCanAddMember(type, executorId, UUID.randomUUID(), ClassRole.STUDENT))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"WEG", "STUDENT", "TEACHER", "REPRESENTATIVE"})
    @DisplayName("deve lançar UserPermissionDeniedException para executores não autorizados")
    void shouldThrowForDisallowedExecutors(TypeUser type) {
        assertThatThrownBy(() -> validator.validateExecutorCanAddMember(type, UUID.randomUUID(), UUID.randomUUID(), ClassRole.STUDENT))
                .isInstanceOf(UserPermissionDeniedException.class);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando role é REPRESENTATIVE")
    void shouldThrowWhenRoleIsRepresentative() {
        assertThatThrownBy(() -> validator.validateExecutorCanAddMember(TypeUser.SENAI, UUID.randomUUID(), UUID.randomUUID(), ClassRole.REPRESENTATIVE))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando executor tenta se associar")
    void shouldThrowOnSelfAssociation() {
        UUID sameId = UUID.randomUUID();
        assertThatThrownBy(() -> validator.validateExecutorCanAddMember(TypeUser.SENAI, sameId, sameId, ClassRole.STUDENT))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("não deve lançar exceção quando IDs são diferentes")
    void shouldNotThrowWhenDifferentUsers() {
        assertThatCode(() -> validator.validateExecutorCanAddMember(TypeUser.SENAI, UUID.randomUUID(), UUID.randomUUID(), ClassRole.STUDENT))
                .doesNotThrowAnyException();
    }

    // --- validateTargetUserCanBeAdded ---

    @Test
    @DisplayName("não deve lançar exceção para STUDENT com role STUDENT")
    void shouldNotThrowForStudentWithStudentRole() {
        assertThatCode(() -> validator.validateTargetUserCanBeAdded(targetStudent, ClassRole.STUDENT))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("não deve lançar exceção para TEACHER com role TEACHER")
    void shouldNotThrowForTeacherWithTeacherRole() {
        assertThatCode(() -> validator.validateTargetUserCanBeAdded(targetTeacher, ClassRole.TEACHER))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando usuário está inativo")
    void shouldThrowWhenUserIsInactive() {
        targetStudent.delete(executor);
        assertThatThrownBy(() -> validator.validateTargetUserCanBeAdded(targetStudent, ClassRole.STUDENT))
                .isInstanceOf(ClassMembershipException.class);
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"ADMIN", "SENAI", "WEG", "REPRESENTATIVE"})
    @DisplayName("deve lançar ClassMembershipException para tipos não associáveis como alvo")
    void shouldThrowForDisallowedTargetTypes(TypeUser type) {
        UserEntity disallowed = new UserEntity("User", "user@test.com", "hash", type);
        assertThatThrownBy(() -> validator.validateTargetUserCanBeAdded(disallowed, ClassRole.STUDENT))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException para STUDENT com role TEACHER")
    void shouldThrowForStudentWithTeacherRole() {
        assertThatThrownBy(() -> validator.validateTargetUserCanBeAdded(targetStudent, ClassRole.TEACHER))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException para TEACHER com role STUDENT")
    void shouldThrowForTeacherWithStudentRole() {
        assertThatThrownBy(() -> validator.validateTargetUserCanBeAdded(targetTeacher, ClassRole.STUDENT))
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

    // --- validateExecutorCanPromote ---

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"ADMIN", "SENAI"})
    @DisplayName("não deve lançar exceção quando executor pode promover")
    void shouldNotThrowWhenExecutorCanPromote(TypeUser type) {
        assertThatCode(() -> validator.validateExecutorCanPromote(type))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"WEG", "STUDENT", "TEACHER", "REPRESENTATIVE"})
    @DisplayName("deve lançar UserPermissionDeniedException quando executor não pode promover")
    void shouldThrowWhenExecutorCannotPromote(TypeUser type) {
        assertThatThrownBy(() -> validator.validateExecutorCanPromote(type))
                .isInstanceOf(UserPermissionDeniedException.class);
    }

// --- validateTargetUserForPromotion ---

    @Test
    @DisplayName("não deve lançar exceção quando aluno vinculado como STUDENT é elegível")
    void shouldNotThrowWhenStudentMemberIsEligible() {
        ClassMembershipEntity membership = new ClassMembershipEntity(targetStudent, activeClass, ClassRole.STUDENT);
        assertThatCode(() -> validator.validateTargetUserForPromotion(targetStudent, membership))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando usuário está inativo")
    void shouldThrowWhenTargetUserIsInactiveForPromotion() {
        targetStudent.delete(executor);
        ClassMembershipEntity membership = new ClassMembershipEntity(targetStudent, activeClass, ClassRole.STUDENT);
        assertThatThrownBy(() -> validator.validateTargetUserForPromotion(targetStudent, membership))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando TypeUser não é STUDENT")
    void shouldThrowWhenTargetUserTypeIsNotStudent() {
        ClassMembershipEntity membership = new ClassMembershipEntity(targetTeacher, activeClass, ClassRole.TEACHER);
        assertThatThrownBy(() -> validator.validateTargetUserForPromotion(targetTeacher, membership))
                .isInstanceOf(ClassMembershipException.class);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando classRole do vínculo não é STUDENT")
    void shouldThrowWhenMembershipRoleIsNotStudent() {
        ClassMembershipEntity membership = new ClassMembershipEntity(targetTeacher, activeClass, ClassRole.TEACHER);
        assertThatThrownBy(() -> validator.validateTargetUserForPromotion(targetStudent, membership))
                .isInstanceOf(ClassMembershipException.class);
    }

// --- validateRepresentativeSlotAvailable ---

    @Test
    @DisplayName("não deve lançar exceção quando há slot disponível")
    void shouldNotThrowWhenSlotIsAvailable() {
        assertThatCode(() -> validator.validateRepresentativeSlotAvailable(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando limite de representantes foi atingido")
    void shouldThrowWhenRepresentativeLimitReached() {
        assertThatThrownBy(() -> validator.validateRepresentativeSlotAvailable(2L))
                .isInstanceOf(ClassMembershipException.class);
    }

    // --- validateExecutorCanDemote ---

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"ADMIN", "SENAI"})
    @DisplayName("não deve lançar exceção quando executor pode remover representante")
    void shouldNotThrowWhenExecutorCanDemote(TypeUser type) {
        assertThatCode(() -> validator.validateExecutorCanDemote(type))
                .doesNotThrowAnyException();
    }

        @ParameterizedTest
        @EnumSource(value = TypeUser.class, names = {"WEG", "STUDENT", "TEACHER", "REPRESENTATIVE"})
        @DisplayName("deve lançar UserPermissionDeniedException quando executor não pode remover representante")
        void shouldThrowWhenExecutorCannotDemote(TypeUser type) {
            assertThatThrownBy(() -> validator.validateExecutorCanDemote(type))
                    .isInstanceOf(UserPermissionDeniedException.class);
        }

    // --- validateTargetUserForDemotion ---

    @Test
    @DisplayName("não deve lançar exceção quando vínculo é ativo e classRole é REPRESENTATIVE")
    void shouldNotThrowWhenMembershipIsActiveAndRepresentativeForDemotion() {
        ClassMembershipEntity membership = mock(ClassMembershipEntity.class);
        when(membership.isActive()).thenReturn(true);
        when(membership.getClassRole()).thenReturn(ClassRole.REPRESENTATIVE);

        assertThatCode(() -> validator.validateTargetUserForDemotion(membership))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando usuário ou turma estão inativos/deletados ao remover representante")
    void shouldThrowWhenMembershipIsInactiveForDemotion() {
        ClassMembershipEntity membership = mock(ClassMembershipEntity.class);
        when(membership.isActive()).thenReturn(false);

        assertThatThrownBy(() -> validator.validateTargetUserForDemotion(membership))
                .isInstanceOf(ClassMembershipException.class)
                .hasMessageContaining("User or Class is inactive or deleted");
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando classRole não é REPRESENTATIVE ao remover representante")
    void shouldThrowWhenMembershipRoleIsNotRepresentativeForDemotion() {
        ClassMembershipEntity membership = mock(ClassMembershipEntity.class);
        when(membership.isActive()).thenReturn(true);
        when(membership.getClassRole()).thenReturn(ClassRole.STUDENT);

        assertThatThrownBy(() -> validator.validateTargetUserForDemotion(membership))
                .isInstanceOf(ClassMembershipException.class)
                .hasMessageContaining("Only memberships with role REPRESENTATIVE");
    }

}
