package com.portal.conecta.hub.shared.config;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DevDataInitializerTest {

    private final DevDataInitializer initializer = new DevDataInitializer();
    private final RoomRepository roomRepository = mock(RoomRepository.class);
    private final UserEntity admin = mock(UserEntity.class);
    private final List<RoomEntity> rooms = new ArrayList<>();

    @Test
    void shouldSeedCatalogRoomsWithExpectedTypesIdempotently() {
        when(roomRepository.findAll()).thenReturn(rooms);
        rooms.add(new RoomEntity(202, TypeRoom.CLASSROOM));
        when(roomRepository.save(org.mockito.ArgumentMatchers.any(RoomEntity.class)))
                .thenAnswer(invocation -> {
                    RoomEntity room = invocation.getArgument(0);
                    rooms.add(room);
                    return room;
                });

        initializer.seedActiveRooms(roomRepository, admin);
        initializer.seedActiveRooms(roomRepository, admin);

        assertEquals(16, rooms.size());
        List<Integer> classroomNumbers = List.of(201, 203);
        classroomNumbers.forEach(number -> assertRoomType(number, TypeRoom.CLASSROOM));

        assertRoomType(101, TypeRoom.ELECTROTECHNICS_LABORATORY);
        List<Integer> electronicsLaboratoryNumbers = List.of(102, 103);
        electronicsLaboratoryNumbers.forEach(number -> assertRoomType(number, TypeRoom.ELECTRONICS_LABORATORY));
        assertRoomType(110, TypeRoom.CNC_SIMULATION);
        List<Integer> computerLaboratoryNumbers = List.of(109, 202, 204, 205, 206, 207, 211, 212, 213, 214);
        computerLaboratoryNumbers.forEach(number -> assertRoomType(number, TypeRoom.COMPUTER_LABORATORY));

        assertMockRoomId("00000000-0000-0000-0000-000000000101", 101);
        assertMockRoomId("00000000-0000-0000-0000-000000000214", 214);
    }

    private void assertRoomType(int number, TypeRoom expectedType) {
        RoomEntity room = rooms.stream()
                .filter(candidate -> candidate.getNumber().equals(number))
                .findFirst()
                .orElseThrow();

        assertEquals(expectedType, room.getTypeRoom());
        assertTrue(room.isActive());
    }

    private void assertMockRoomId(String id, int number) {
        verify(roomRepository, times(2)).updateIdByNumber(UUID.fromString(id), number);
    }
}
