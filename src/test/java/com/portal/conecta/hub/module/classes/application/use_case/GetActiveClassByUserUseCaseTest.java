package com.portal.conecta.hub.module.classes.application.use_case;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ActiveClassNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GetActiveClassByUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClassMembershipRepository classMembershipRepository;

    private GetActiveClassByUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetActiveClassByUserUseCase(userRepository, classMembershipRepository);
    }

    @Test
    @DisplayName("deve retornar UUID da turma quando usuário for aluno com vínculo ativo")
    void shouldReturnActiveClassWhenUserIsStudent() {

        UUID userId = UUID.randomUUID();
        UserEntity user = activeUser(userId, TypeUser.STUDENT);
        ClassEntity classEntity = activeClass();
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);

        when(userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(Optional.of(user));
        when(classMembershipRepository.findEligibleActiveByUserIdAndRoles(eq(userId), any()))
                .thenReturn(List.of(membership));

        UUID result = useCase.execute(new GetActiveClassByUserCommand(userId));

        assertThat(result).isEqualTo(classEntity.getId());
    }

    @Test
    @DisplayName("deve retornar UUID da turma quando usuário for representante com vínculo ativo")
    void shouldReturnActiveClassWhenUserIsRepresentative() {
        UUID userId = UUID.randomUUID();
        UserEntity user = activeUser(userId, TypeUser.REPRESENTATIVE);
        ClassEntity classEntity = activeClass();
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.REPRESENTATIVE);

        when(userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(Optional.of(user));
        when(classMembershipRepository.findEligibleActiveByUserIdAndRoles(eq(userId), any()))
                .thenReturn(List.of(membership));

        UUID result = useCase.execute(new GetActiveClassByUserCommand(userId));

        assertThat(result).isEqualTo(classEntity.getId());
    }

    @Test
    @DisplayName("deve lançar ActiveClassNotFoundException quando não houver vínculo elegível")
    void shouldThrowActiveClassNotFoundWhenNoEligibleMembership() {
        UUID userId = UUID.randomUUID();
        UserEntity user = activeUser(userId, TypeUser.STUDENT);

        when(userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(Optional.of(user));
        when(classMembershipRepository.findEligibleActiveByUserIdAndRoles(eq(userId), any()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new GetActiveClassByUserCommand(userId)))
                .isInstanceOf(ActiveClassNotFoundException.class);
    }

    @Test
    @DisplayName("deve lançar ActiveClassNotFoundException quando a turma estiver desativada")
    void shouldThrowActiveClassNotFoundWhenClassIsInactive() {
        UUID userId = UUID.randomUUID();
        UserEntity user = activeUser(userId, TypeUser.STUDENT);
        ClassEntity inactiveClass = inactiveClass();
        ClassMembershipEntity membership = new ClassMembershipEntity(user, inactiveClass, ClassRole.STUDENT);

        when(userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(Optional.of(user));
        when(classMembershipRepository.findEligibleActiveByUserIdAndRoles(eq(userId), any()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new GetActiveClassByUserCommand(userId)))
                .isInstanceOf(ActiveClassNotFoundException.class);

        assertThat(membership.getClassEntity().isActive()).isFalse();
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário não existir, estiver inativo ou removido")
    void shouldThrowUserNotFoundWhenUserUnavailable() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetActiveClassByUserCommand(userId)))
                .isInstanceOf(UserNotFoundException.class);

        verify(classMembershipRepository, never()).findEligibleActiveByUserIdAndRoles(any(), any());
    }

    private UserEntity activeUser(UUID userId, TypeUser type) {
        UserEntity user = new UserEntity("Nome Teste", "teste@teste.com", "hash", type);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private ClassEntity activeClass() {
        CourseEntity course = CourseEntity.create("Curso Teste", "CT01");
        ReflectionTestUtils.setField(course, "id", UUID.randomUUID());

        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, null);
        ReflectionTestUtils.setField(classEntity, "id", UUID.randomUUID());
        return classEntity;
    }

    private ClassEntity inactiveClass() {
        CourseEntity course = CourseEntity.create("Curso Teste", "CT01");
        ReflectionTestUtils.setField(course, "id", UUID.randomUUID());

        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, null);
        ReflectionTestUtils.setField(classEntity, "id", UUID.randomUUID());
        classEntity.deactivate(null);
        return classEntity;
    }
}