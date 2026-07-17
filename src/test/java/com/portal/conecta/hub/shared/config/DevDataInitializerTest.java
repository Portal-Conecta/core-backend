package com.portal.conecta.hub.shared.config;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DevDataInitializerTest {

    private final DevDataInitializer initializer = new DevDataInitializer();
    private final RoomRepository roomRepository = mock(RoomRepository.class);
    private final UserEntity admin = mock(UserEntity.class);
    private final List<RoomEntity> rooms = new ArrayList<>();

    @Test
    void shouldSeedCatalogRoomsWithExpectedTypesIdempotently() {
        when(roomRepository.findAll()).thenReturn(rooms);
        when(roomRepository.save(org.mockito.ArgumentMatchers.any(RoomEntity.class)))
                .thenAnswer(invocation -> {
                    RoomEntity room = invocation.getArgument(0);
                    rooms.add(room);
                    return room;
                });

        initializer.seedActiveRooms(roomRepository, admin);
        initializer.seedActiveRooms(roomRepository, admin);

        assertEquals(15, rooms.size());
        assertRoomType(101, TypeRoom.LABORATORY);
        assertRoomType(102, TypeRoom.LABORATORY);
        assertRoomType(103, TypeRoom.LABORATORY);
        assertRoomType(109, TypeRoom.LABORATORY);
        assertRoomType(110, TypeRoom.LABORATORY);

        List<Integer> classroomNumbers = List.of(201, 202, 203, 204, 205, 206, 207, 212, 213, 214);
        classroomNumbers.forEach(number -> assertRoomType(number, TypeRoom.CLASSROOM));
    }

    private void assertRoomType(int number, TypeRoom expectedType) {
        RoomEntity room = rooms.stream()
                .filter(candidate -> candidate.getNumber().equals(number))
                .findFirst()
                .orElseThrow();

        assertEquals(expectedType, room.getTypeRoom());
        assertTrue(room.isActive());
    }
}
