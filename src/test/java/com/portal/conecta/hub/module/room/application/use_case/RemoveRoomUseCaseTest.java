package com.portal.conecta.hub.module.room.application.use_case;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.room.application.command.RemoveRoomCommand;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class RemoveRoomUseCaseTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GetRoomByIdUseCase getRoomByIdUseCase;

    @Mock
    private RequestContextProvider contextProvider;

    private RemoveRoomUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RemoveRoomUseCase(
                roomRepository,
                userRepository,
                getRoomByIdUseCase,
                contextProvider,
                new RoomPermissionValidator()
        );
    }

    private RoomEntity activeRoom(UUID id) {
        RoomEntity room = new RoomEntity(101, TypeRoom.CLASSROOM);
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    private RoomEntity removedRoom(UUID id, UserEntity deletedBy) {
        RoomEntity room = activeRoom(id);
        room.delete(deletedBy);
        return room;
    }

    @Test
    void adminRemovesRoomSuccessfully() {
        UUID adminId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity admin = new UserEntity("Admin", "admin@portal.test", "hash", TypeUser.ADMIN);
        RoomEntity room = activeRoom(roomId);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(new RemoveRoomCommand(roomId));

        assertNotNull(room.getDeletedAt());
        verify(roomRepository).save(room);
    }

    @Test
    void senaiRemovesRoomSuccessfully() {
        UUID senaiId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity senai = new UserEntity("Senai", "senai@sesisenai.org.br", "hash", TypeUser.SENAI);
        RoomEntity room = activeRoom(roomId);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(senaiId, TypeUser.SENAI, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(userRepository.findById(senaiId)).thenReturn(Optional.of(senai));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(new RemoveRoomCommand(roomId));

        assertNotNull(room.getDeletedAt());
        verify(roomRepository).save(room);
    }

    @Test
    void wegRemovesRoomSuccessfully() {
        UUID wegId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity weg = new UserEntity("Weg", "weg@weg.net", "hash", TypeUser.WEG);
        RoomEntity room = activeRoom(roomId);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(wegId, TypeUser.WEG, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(userRepository.findById(wegId)).thenReturn(Optional.of(weg));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(new RemoveRoomCommand(roomId));

        assertNotNull(room.getDeletedAt());
    }

    @Test
    void studentCannotRemoveRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new RemoveRoomCommand(UUID.randomUUID())));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void teacherCannotRemoveRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.TEACHER, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new RemoveRoomCommand(UUID.randomUUID())));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void representativeCannotRemoveRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.REPRESENTATIVE, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new RemoveRoomCommand(UUID.randomUUID())));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void throwsNotFoundWhenRoomDoesNotExist() {
        UUID roomId = UUID.randomUUID();

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of()));
        when(getRoomByIdUseCase.execute(roomId))
                .thenThrow(new RoomNotFoundException("Room not found: " + roomId));

        assertThrows(RoomNotFoundException.class,
                () -> useCase.execute(new RemoveRoomCommand(roomId)));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void throwsInvalidDataWhenRoomAlreadyRemoved() {
        UUID adminId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity admin = new UserEntity("Admin", "admin@portal.test", "hash", TypeUser.ADMIN);
        RoomEntity room = removedRoom(roomId, admin);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);

        assertThrows(InvalidRoomDataException.class,
                () -> useCase.execute(new RemoveRoomCommand(roomId)));

        verify(roomRepository, never()).save(any());
    }
}