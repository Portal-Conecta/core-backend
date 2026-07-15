package com.portal.conecta.hub.module.classes.domain.model;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClassMembershipEntityTest {
    private final UserEntity user = new UserEntity("Student", "student@test.local", "hash", TypeUser.STUDENT);
    private final ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, CourseEntity.create("Course", "COURSE"), null);

    @Test void constructorRequiresValuesAndConnectsBothSides() {
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);
        assertSame(user, membership.getUser());
        assertSame(classEntity, membership.getClassEntity());
        assertTrue(user.getClassMemberships().contains(membership));
        assertTrue(classEntity.getClassMemberships().contains(membership));
        assertThrows(NullPointerException.class, () -> new ClassMembershipEntity(null, classEntity, ClassRole.STUDENT));
        assertThrows(NullPointerException.class, () -> new ClassMembershipEntity(user, null, ClassRole.STUDENT));
        assertThrows(NullPointerException.class, () -> new ClassMembershipEntity(user, classEntity, null));
    }

    @Test void promotionAndDemotionSynchronizeMembershipAndUser() {
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);
        UserEntity executor = new UserEntity("Admin", "admin@test.local", "hash", TypeUser.ADMIN);
        membership.promoteToRepresentative(executor);
        assertEquals(ClassRole.REPRESENTATIVE, membership.getClassRole());
        assertEquals(TypeUser.REPRESENTATIVE, user.getTypeUser());
        assertSame(executor, user.getUpdatedBy());
        membership.demoteToStudent(executor);
        assertEquals(ClassRole.STUDENT, membership.getClassRole());
        assertEquals(TypeUser.STUDENT, user.getTypeUser());
    }

    @Test void activeRequiresActiveAndNonDeletedUserAndNonDeletedClass() {
        assertTrue(new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT).isActive());
        UserEntity inactiveUser = UserEntity.createPendingActivation("Inactive", "inactive@test.local", "hash", TypeUser.STUDENT, null);
        assertFalse(new ClassMembershipEntity(inactiveUser, classEntity, ClassRole.STUDENT).isActive());

        ReflectionTestUtils.setField(user, "deletedAt", Instant.now());
        assertFalse(new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT).isActive());

        UserEntity activeUser = new UserEntity("Other", "other@test.local", "hash", TypeUser.STUDENT);
        ClassEntity deletedClass = ClassEntity.create(Shift.FULL_AM_PM, 2, CourseEntity.create("Course", "COURSE"), null);
        deletedClass.delete(null);
        assertFalse(new ClassMembershipEntity(activeUser, deletedClass, ClassRole.STUDENT).isActive());
    }

    @Test void equalityUsesCompositeIdentity() {
        ClassMembershipEntity first = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);
        ClassMembershipEntity same = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);
        UserEntity anotherUser = new UserEntity("Other", "other@test.local", "hash", TypeUser.STUDENT);
        ClassMembershipEntity other = new ClassMembershipEntity(anotherUser, classEntity, ClassRole.STUDENT);
        ClassMembershipId id = new ClassMembershipId(UUID.randomUUID(), UUID.randomUUID());
        ReflectionTestUtils.setField(first, "id", id);
        ReflectionTestUtils.setField(same, "id", id);
        ReflectionTestUtils.setField(other, "id", new ClassMembershipId(UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(first, first);
        assertEquals(first, same);
        assertNotEquals(first, other);
        assertNotEquals(first, null);
        assertNotEquals(first, user);
        ReflectionTestUtils.setField(first, "id", null);
        assertNotEquals(first, same);
    }

    @Test void buildsCompositeIdImmediatelyWhenEntitiesArePersisted() {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(classEntity, "id", classId);
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);
        assertEquals(userId, membership.getId().getUserId());
        assertEquals(classId, membership.getId().getClassId());
        ReflectionTestUtils.invokeMethod(membership, "prePersist");
        assertNotNull(membership.getCreatedAt());
    }

    @Test void prePersistRequiresBothAssociatedEntitiesToHaveIds() {
        ClassMembershipEntity withoutIds = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(withoutIds, "prePersist"));

        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ClassMembershipEntity withoutClassId = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(withoutClassId, "prePersist"));
    }
}
