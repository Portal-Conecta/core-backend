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
    void senaiCanCreateRoom() {
        assertTrue(validator.canCreate(TypeUser.SENAI));
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
}