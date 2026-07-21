package com.portal.conecta.hub.shared.config;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class DevDataInitializerJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    private final DevDataInitializer initializer = new DevDataInitializer();

    @Test
    void shouldPersistManualIdsForCatalogRooms() {
        initializer.seedActiveRooms(roomRepository, null);
        entityManager.clear();

        assertRoomId(101, "00000000-0000-0000-0000-000000000101");
        assertRoomId(214, "00000000-0000-0000-0000-000000000214");
    }

    private void assertRoomId(int number, String expectedId) {
        RoomEntity room = roomRepository.findAll().stream()
                .filter(candidate -> candidate.getNumber().equals(number))
                .findFirst()
                .orElseThrow();

        assertEquals(UUID.fromString(expectedId), room.getId());
    }
}
