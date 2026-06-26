package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.application.command.RestoreRoomCommand;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.room.domain.validator.RoomPermissionValidator;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestoreRoomUseCaseTest {

    @Mock private RoomRepository roomRepository;
    @Mock private UserRepository userRepository;
    @Mock private RequestContextProvider requestContextProvider;
    @Mock private RoomPermissionValidator roomPermissionValidator;

    @InjectMocks
    private RestoreRoomUseCase restoreRoomUseCase;

    private UUID roomId;
    private UUID userId;
    private RequestContext adminContext;
    private UserEntity executor;
    private RoomEntity deletedRoom;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        userId = UUID.randomUUID();

        adminContext = new RequestContext(userId, TypeUser.ADMIN, List.of());
        executor = mock(UserEntity.class);

        deletedRoom = RoomEntity.create(101, TypeRoom.CLASSROOM, executor);
        deletedRoom.delete(executor);
    }

    @Test
    @DisplayName("deve restaurar sala removida com sucesso")
    void shouldRestoreRoomSuccessfully() {
        when(requestContextProvider.getRequestContext()).thenReturn(adminContext);
        when(roomPermissionValidator.canRestore(TypeUser.ADMIN)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(deletedRoom));
        when(userRepository.findById(userId)).thenReturn(Optional.of(executor));
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RoomEntity result = restoreRoomUseCase.execute(new RestoreRoomCommand(roomId));

        assertThat(result.isActive()).isTrue();
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        verify(roomRepository).save(deletedRoom);
    }

    @Test
    @DisplayName("deve lançar RoomPermissionDeniedException quando usuário não tem permissão")
    void shouldThrowWhenUserHasNoPermission() {
        RequestContext studentContext = new RequestContext(userId, TypeUser.STUDENT, List.of());
        when(requestContextProvider.getRequestContext()).thenReturn(studentContext);
        when(roomPermissionValidator.canRestore(TypeUser.STUDENT)).thenReturn(false);

        assertThatThrownBy(() -> restoreRoomUseCase.execute(new RestoreRoomCommand(roomId)))
                .isInstanceOf(RoomPermissionDeniedException.class);

        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("deve lançar RoomNotFoundException quando sala não existe")
    void shouldThrowWhenRoomNotFound() {
        when(requestContextProvider.getRequestContext()).thenReturn(adminContext);
        when(roomPermissionValidator.canRestore(TypeUser.ADMIN)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restoreRoomUseCase.execute(new RestoreRoomCommand(roomId)))
                .isInstanceOf(RoomNotFoundException.class);

        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar InvalidRoomDataException quando sala já está ativa")
    void shouldThrowWhenRoomIsAlreadyActive() {
        RoomEntity activeRoom = RoomEntity.create(101, TypeRoom.CLASSROOM, executor);

        when(requestContextProvider.getRequestContext()).thenReturn(adminContext);
        when(roomPermissionValidator.canRestore(TypeUser.ADMIN)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));

        assertThatThrownBy(() -> restoreRoomUseCase.execute(new RestoreRoomCommand(roomId)))
                .isInstanceOf(InvalidRoomDataException.class)
                .hasMessageContaining("não está removida");

        verify(roomRepository, never()).save(any());
    }
}