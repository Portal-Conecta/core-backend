package com.portal.conecta.hub.module.room.domain.model;

import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class RoomEntityTest {
    private final UserEntity creator = new UserEntity("Admin", "admin@test.local", "hash", TypeUser.ADMIN);

    @Test void createSetsAuditAndUpdateChangesOnlyDifferentValues() {
        RoomEntity room = RoomEntity.create(101, TypeRoom.CLASSROOM, creator);
        assertSame(creator, room.getCreatedBy());
        assertSame(creator, room.getUpdatedBy());
        UserEntity updater = new UserEntity("Other", "other@test.local", "hash", TypeUser.WEG);
        assertEquals(List.of("number", "typeRoom"), room.update(102, TypeRoom.COMPUTER_LABORATORY, updater));
        assertEquals(102, room.getNumber());
        assertEquals(TypeRoom.COMPUTER_LABORATORY, room.getTypeRoom());
        assertSame(updater, room.getUpdatedBy());
        assertTrue(room.update(null, null, creator).isEmpty());
        assertTrue(room.update(102, TypeRoom.COMPUTER_LABORATORY, creator).isEmpty());
    }

    @Test void deleteAndRestoreToggleSoftDeleteAndDeletedRoomCannotBeUpdated() {
        RoomEntity room = RoomEntity.create(101, TypeRoom.CLASSROOM, creator);
        room.delete(creator);
        assertFalse(room.isActive());
        assertSame(creator, room.getDeletedBy());
        assertThrows(InvalidRoomDataException.class, () -> room.update(102, null, creator));
        room.restore(creator);
        assertTrue(room.isActive());
        assertNull(room.getDeletedBy());
        assertSame(creator, room.getUpdatedBy());
    }

    @Test void constructorRejectsMissingRequiredFields() {
        assertThrows(NullPointerException.class, () -> new RoomEntity(null, TypeRoom.CLASSROOM));
        assertThrows(NullPointerException.class, () -> new RoomEntity(1, null));
    }

    @Test void equalityUsesPersistentIdentity() {
        RoomEntity first = RoomEntity.create(1, TypeRoom.CLASSROOM, creator);
        RoomEntity same = RoomEntity.create(2, TypeRoom.CLASSROOM, creator);
        RoomEntity other = RoomEntity.create(3, TypeRoom.CLASSROOM, creator);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(first, "id", id);
        ReflectionTestUtils.setField(same, "id", id);
        ReflectionTestUtils.setField(other, "id", UUID.randomUUID());
        assertEquals(first, first);
        assertEquals(first, same);
        assertNotEquals(first, other);
        assertNotEquals(first, null);
        assertNotEquals(first, "room");
        assertNotEquals(RoomEntity.create(4, TypeRoom.CLASSROOM, creator), same);
    }
}
