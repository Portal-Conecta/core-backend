package com.portal.conecta.hub.module.room.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.room.application.command.UpdateRoomCommand;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNumberAlreadyInUseException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UpdateRoomUseCaseTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GetRoomByIdUseCase getRoomByIdUseCase;

    @Mock
    private RequestContextProvider contextProvider;

    private UpdateRoomUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateRoomUseCase(
                roomRepository,
                userRepository,
                getRoomByIdUseCase,
                new RoomPermissionValidator(),
                contextProvider
        );
    }

    private RoomEntity activeRoom(UUID id, Integer number, TypeRoom type) {
        RoomEntity room = new RoomEntity(number, type);
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    @Test
    void adminUpdatesRoomSuccessfully() {
        UUID adminId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity admin = new UserEntity("Admin", "admin@portal.test", "hash", TypeUser.ADMIN);
        RoomEntity room = activeRoom(roomId, 101, TypeRoom.CLASSROOM);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(roomRepository.existsByNumberAndIdNot(204, roomId)).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new UpdateRoomCommand(roomId, 204, TypeRoom.LABORATORY));

        assertEquals(204, result.getNumber());
        assertEquals(TypeRoom.LABORATORY, result.getTypeRoom());
        assertEquals(admin, result.getUpdatedBy());
        verify(roomRepository).save(room);
    }

    @Test
    void senaiUpdatesRoomSuccessfully() {
        UUID senaiId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity senai = new UserEntity("Senai", "senai@sesisenai.org.br", "hash", TypeUser.SENAI);
        RoomEntity room = activeRoom(roomId, 101, TypeRoom.CLASSROOM);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(senaiId, TypeUser.SENAI, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(roomRepository.existsByNumberAndIdNot(202, roomId)).thenReturn(false);
        when(userRepository.findById(senaiId)).thenReturn(Optional.of(senai));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new UpdateRoomCommand(roomId, 202, TypeRoom.AUDITORIUM));

        assertEquals(202, result.getNumber());
        assertEquals(TypeRoom.AUDITORIUM, result.getTypeRoom());
    }

    @Test
    void wegUpdatesRoomSuccessfully() {
        UUID wegId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity weg = new UserEntity("Weg", "weg@weg.net", "hash", TypeUser.WEG);
        RoomEntity room = activeRoom(roomId, 101, TypeRoom.CLASSROOM);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(wegId, TypeUser.WEG, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(roomRepository.existsByNumberAndIdNot(303, roomId)).thenReturn(false);
        when(userRepository.findById(wegId)).thenReturn(Optional.of(weg));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new UpdateRoomCommand(roomId, 303, TypeRoom.OTHER));

        assertEquals(303, result.getNumber());
    }

    @Test
    void updatesOnlyType() {
        UUID adminId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity admin = new UserEntity("Admin", "admin@portal.test", "hash", TypeUser.ADMIN);
        RoomEntity room = activeRoom(roomId, 101, TypeRoom.CLASSROOM);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new UpdateRoomCommand(roomId, null, TypeRoom.LABORATORY));

        assertEquals(101, result.getNumber());
        assertEquals(TypeRoom.LABORATORY, result.getTypeRoom());
        verify(roomRepository, never()).existsByNumberAndIdNot(any(), any());
    }

    @Test
    void updatesOnlyNumber() {
        UUID adminId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity admin = new UserEntity("Admin", "admin@portal.test", "hash", TypeUser.ADMIN);
        RoomEntity room = activeRoom(roomId, 101, TypeRoom.CLASSROOM);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(roomRepository.existsByNumberAndIdNot(505, roomId)).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new UpdateRoomCommand(roomId, 505, null));

        assertEquals(505, result.getNumber());
        assertEquals(TypeRoom.CLASSROOM, result.getTypeRoom());
    }

    @Test
    void doesNotCheckUniquenessWhenNumberIsUnchanged() {
        UUID adminId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UserEntity admin = new UserEntity("Admin", "admin@portal.test", "hash", TypeUser.ADMIN);
        RoomEntity room = activeRoom(roomId, 101, TypeRoom.CLASSROOM);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new UpdateRoomCommand(roomId, 101, TypeRoom.LABORATORY));

        verify(roomRepository, never()).existsByNumberAndIdNot(any(), any());
        assertEquals(TypeRoom.LABORATORY, result.getTypeRoom());
    }

    @Test
    void studentCannotUpdateRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new UpdateRoomCommand(UUID.randomUUID(), 101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void teacherCannotUpdateRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.TEACHER, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new UpdateRoomCommand(UUID.randomUUID(), 101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void representativeCannotUpdateRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.REPRESENTATIVE, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new UpdateRoomCommand(UUID.randomUUID(), 101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void throwsInvalidDataWhenNoFieldProvided() {
        assertThrows(InvalidRoomDataException.class,
                () -> useCase.execute(new UpdateRoomCommand(UUID.randomUUID(), null, null)));

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
                () -> useCase.execute(new UpdateRoomCommand(roomId, 101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void throwsConflictWhenNumberAlreadyExists() {
        UUID adminId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        RoomEntity room = activeRoom(roomId, 101, TypeRoom.CLASSROOM);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);
        when(roomRepository.existsByNumberAndIdNot(999, roomId)).thenReturn(true);

        assertThrows(RoomNumberAlreadyInUseException.class,
                () -> useCase.execute(new UpdateRoomCommand(roomId, 999, null)));

        verify(roomRepository, never()).save(any());
    }
}