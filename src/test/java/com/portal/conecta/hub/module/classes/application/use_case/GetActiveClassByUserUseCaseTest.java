package com.portal.conecta.hub.module.classes.application.use_case;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
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
    @DisplayName("deve retornar memberships quando usuário for aluno com vínculo ativo")
    void shouldReturnMembershipsWhenUserIsStudent() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, TypeUser.STUDENT);
        ClassEntity classEntity = buildActiveClass();
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);

        when(userRepository.existsByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(true);
        when(classMembershipRepository.findActiveByUserId(userId)).thenReturn(List.of(membership));

        List<ClassMembershipEntity> result = useCase.execute(new GetActiveClassByUserCommand(userId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClassRole()).isEqualTo(ClassRole.STUDENT);
    }

    @Test
    @DisplayName("deve retornar memberships quando usuário for representante com vínculo ativo")
    void shouldReturnMembershipsWhenUserIsRepresentative() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, TypeUser.REPRESENTATIVE);
        ClassEntity classEntity = buildActiveClass();
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.REPRESENTATIVE);

        when(userRepository.existsByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(true);
        when(classMembershipRepository.findActiveByUserId(userId)).thenReturn(List.of(membership));

        List<ClassMembershipEntity> result = useCase.execute(new GetActiveClassByUserCommand(userId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClassRole()).isEqualTo(ClassRole.REPRESENTATIVE);
    }

    @Test
    @DisplayName("deve retornar lista vazia quando usuário não tiver vínculos ativos")
    void shouldReturnEmptyListWhenNoActiveMemberships() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(true);
        when(classMembershipRepository.findActiveByUserId(userId)).thenReturn(List.of());

        List<ClassMembershipEntity> result = useCase.execute(new GetActiveClassByUserCommand(userId));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário não existir, estiver inativo ou removido")
    void shouldThrowUserNotFoundWhenUserUnavailable() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByIdAndDeletedAtIsNullAndActiveTrue(userId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new GetActiveClassByUserCommand(userId)))
                .isInstanceOf(UserNotFoundException.class);

        verify(classMembershipRepository, never()).findActiveByUserId(any());
    }

    private UserEntity buildUser(UUID userId, TypeUser type) {
        UserEntity user = new UserEntity("Nome Teste", "teste@teste.com", "hash", type);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private ClassEntity buildActiveClass() {
        CourseEntity course = CourseEntity.create("Curso Teste", "CT01");
        ReflectionTestUtils.setField(course, "id", UUID.randomUUID());

        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, null);
        ReflectionTestUtils.setField(classEntity, "id", UUID.randomUUID());
        return classEntity;
    }
}