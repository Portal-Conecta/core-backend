package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.user.application.command.UpdateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionValidator permissionValidator;

    @Mock
    private RequestContextProvider requestProvider;

    @InjectMocks
    private UpdateUserUseCase useCase;

    private UUID requesterId;
    private UUID targetId;
    private UserEntity requester;
    private UserEntity target;
    private RequestContext context;

    @BeforeEach
    void setUp() {
        requesterId = UUID.randomUUID();
        targetId = UUID.randomUUID();

        requester = new UserEntity("Requester", "requester@test.com", "hash", TypeUser.ADMIN);

        // spy em vez de mock — executa os métodos reais
        target = spy(new UserEntity("Target", "target@test.com", "hash", TypeUser.STUDENT));
        doReturn(targetId).when(target).getId();

        context = new RequestContext(requesterId, TypeUser.ADMIN, List.of());
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando command é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(userRepository, permissionValidator, requestProvider);
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário alvo não existe")
    void shouldThrowWhenTargetUserNotFound() {
        UpdateUserCommand command = new UpdateUserCommand(targetId, "Nome", null, null);

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(targetId.toString());

        verify(userRepository).findById(targetId);
    }

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando usuário não tem permissão")
    void shouldThrowWhenUserHasNoPermission() {
        UpdateUserCommand command = new UpdateUserCommand(targetId, "Nome", null, null);

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        doThrow(new UserPermissionDeniedException("no permission"))
                .when(permissionValidator).validateCanEdit(
                        requesterId, TypeUser.ADMIN, targetId, TypeUser.STUDENT
                );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar EmailAlreadyInUseException quando email já está em uso por outro usuário")
    void shouldThrowWhenEmailAlreadyInUse() {
        UpdateUserCommand command = new UpdateUserCommand(targetId, null, "duplicate@test.com", null);

        AuthUser duplicate = mock(AuthUser.class);
        when(duplicate.getId()).thenReturn(UUID.randomUUID());

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        doNothing().when(permissionValidator).validateCanEdit(any(), any(), any(), any());
        when(userRepository.findByEmail("duplicate@test.com")).thenReturn(Optional.of(duplicate));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmailAlreadyInUseException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("não deve lançar erro quando email enviado pertence ao próprio usuário alvo")
    void shouldNotThrowWhenEmailBelongsToTarget() {
        UpdateUserCommand command = new UpdateUserCommand(targetId, null, "target@test.com", null);

        AuthUser authTarget = mock(AuthUser.class);
        when(authTarget.getId()).thenReturn(targetId);

        UserEntity targetMock = mock(UserEntity.class);
        when(targetMock.getId()).thenReturn(targetId);
        when(targetMock.getTypeUser()).thenReturn(TypeUser.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetMock));
        doNothing().when(permissionValidator).validateCanEdit(any(), any(), any(), any());
        when(userRepository.findByEmail("target@test.com")).thenReturn(Optional.of(authTarget));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserEntity result = useCase.execute(command);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário autenticado não existe no banco")
    void shouldThrowWhenAuthenticatedUserNotFound() {
        UpdateUserCommand command = new UpdateUserCommand(targetId, "Nome", null, null);

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        doNothing().when(permissionValidator).validateCanEdit(any(), any(), any(), any());
        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(requesterId.toString());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve atualizar nome com sucesso")
    void shouldUpdateNameSuccessfully() {
        UpdateUserCommand command = new UpdateUserCommand(targetId, "Novo Nome", null, null);

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        doNothing().when(permissionValidator).validateCanEdit(any(), any(), any(), any());
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserEntity result = useCase.execute(command);

        assertThat(result.getName()).isEqualTo("Novo Nome");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("deve atualizar email com sucesso quando email não está em uso")
    void shouldUpdateEmailSuccessfully() {
        UpdateUserCommand command = new UpdateUserCommand(targetId, null, "new@test.com", null);

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        doNothing().when(permissionValidator).validateCanEdit(any(), any(), any(), any());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserEntity result = useCase.execute(command);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("deve atualizar avatarUrl com sucesso")
    void shouldUpdateAvatarUrlSuccessfully() {
        UpdateUserCommand command = new UpdateUserCommand(targetId, null, null, "https://avatar.url/img.png");

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        doNothing().when(permissionValidator).validateCanEdit(any(), any(), any(), any());
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserEntity result = useCase.execute(command);

        assertThat(result.getAvatarUrl()).isEqualTo("https://avatar.url/img.png");
        verify(userRepository).save(any(UserEntity.class));
    }
}