package com.portal.conecta.hub.module.room.application.use_case;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.portal.conecta.hub.module.room.application.command.CreateRoomCommand;
import com.portal.conecta.hub.module.room.domain.exception.RoomNumberAlreadyInUseException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.room.domain.validator.RoomPermissionValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateRoomUseCaseTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestContextProvider contextProvider;

    private CreateRoomUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateRoomUseCase(
                roomRepository,
                userRepository,
                new RoomPermissionValidator(),
                contextProvider
        );
    }



    @Test
    void adminCreatesRoomSuccessfully() {
        UUID adminId = UUID.randomUUID();
        UserEntity admin = new UserEntity("Admin", "admin@portal.test", "hash", TypeUser.ADMIN);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(roomRepository.existsByNumber(101)).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new CreateRoomCommand(101, TypeRoom.CLASSROOM));

        assertNotNull(result);
        assertEquals(101, result.getNumber());
        assertEquals(TypeRoom.CLASSROOM, result.getTypeRoom());
        verify(roomRepository).save(any());
    }

    @Test
    void senaiCreatesRoomSuccessfully() {
        UUID senaiId = UUID.randomUUID();
        UserEntity senai = new UserEntity("Senai", "senai@sesisenai.org.br", "hash", TypeUser.SENAI);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(senaiId, TypeUser.SENAI, List.of()));
        when(roomRepository.existsByNumber(202)).thenReturn(false);
        when(userRepository.findById(senaiId)).thenReturn(Optional.of(senai));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new CreateRoomCommand(202, TypeRoom.LABORATORY));

        assertEquals(TypeRoom.LABORATORY, result.getTypeRoom());
    }

    @Test
    void wegCreatesRoomSuccessfully() {
        UUID wegId = UUID.randomUUID();
        UserEntity weg = new UserEntity("Weg", "weg@weg.net", "hash", TypeUser.WEG);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(wegId, TypeUser.WEG, List.of()));
        when(roomRepository.existsByNumber(303)).thenReturn(false);
        when(userRepository.findById(wegId)).thenReturn(Optional.of(weg));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RoomEntity result = useCase.execute(new CreateRoomCommand(303, TypeRoom.AUDITORIUM));

        assertEquals(TypeRoom.AUDITORIUM, result.getTypeRoom());
    }



    @Test
    void studentCannotCreateRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new CreateRoomCommand(101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void teacherCannotCreateRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.TEACHER, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new CreateRoomCommand(101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void representativeCannotCreateRoom() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.REPRESENTATIVE, List.of()));

        assertThrows(RoomPermissionDeniedException.class,
                () -> useCase.execute(new CreateRoomCommand(101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }



    @Test
    void throwsConflictWhenNumberAlreadyExists() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of()));
        when(roomRepository.existsByNumber(101)).thenReturn(true);

        assertThrows(RoomNumberAlreadyInUseException.class,
                () -> useCase.execute(new CreateRoomCommand(101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }


    @Test
    void throwsUserNotFoundWhenAuthenticatedUserNotFound() {
        UUID adminId = UUID.randomUUID();

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(roomRepository.existsByNumber(101)).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> useCase.execute(new CreateRoomCommand(101, TypeRoom.CLASSROOM)));

        verify(roomRepository, never()).save(any());
    }
}