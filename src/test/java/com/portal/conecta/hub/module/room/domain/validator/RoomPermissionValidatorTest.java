package com.portal.conecta.hub.module.room.domain.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.junit.jupiter.api.Test;

class RoomPermissionValidatorTest {

    private final RoomPermissionValidator validator = new RoomPermissionValidator();

    @Test
    void adminCanCreateRoom() {
        assertTrue(validator.canCreate(TypeUser.ADMIN));
    }

    @Test
    void wegCanCreateRoom() {
        assertTrue(validator.canCreate(TypeUser.WEG));
    }

    @Test
    void studentCannotCreateRoom() {
        assertFalse(validator.canCreate(TypeUser.STUDENT));
    }

    @Test
    void teacherCannotCreateRoom() {
        assertFalse(validator.canCreate(TypeUser.TEACHER));
    }

    @Test
    void representativeCannotCreateRoom() {
        assertFalse(validator.canCreate(TypeUser.REPRESENTATIVE));
    }

    @Test
    void nullCannotCreateRoom() {
        assertFalse(validator.canCreate(null));
    }

    @Test
    void adminCanUpdateRoom() {
        assertTrue(validator.canUpdate(TypeUser.ADMIN));
    }

    @Test
    void wegCanUpdateRoom() {
        assertTrue(validator.canUpdate(TypeUser.WEG));
    }

    @Test
    void studentCannotUpdateRoom() {
        assertFalse(validator.canUpdate(TypeUser.STUDENT));
    }

    @Test
    void teacherCannotUpdateRoom() {
        assertFalse(validator.canUpdate(TypeUser.TEACHER));
    }

    @Test
    void representativeCannotUpdateRoom() {
        assertFalse(validator.canUpdate(TypeUser.REPRESENTATIVE));
    }

    @Test
    void nullCannotUpdateRoom() {
        assertFalse(validator.canUpdate(null));
    }
}