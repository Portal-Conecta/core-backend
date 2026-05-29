package com.portal.conecta.hub.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DomainEntityJpaMappingTest {

	private static final String CLASS_ENTITY = "com.portal.conecta.hub.module.classes.domain.model.ClassEntity";
	private static final String COURSE_ENTITY = "com.portal.conecta.hub.module.course.domain.model.CourseEntity";
	private static final String CLASS_ROLE = "com.portal.conecta.hub.module.classes.domain.model.ClassRole";
	private static final String ROOM_ENTITY = "com.portal.conecta.hub.module.room.domain.model.RoomEntity";
	private static final String SHIFT = "com.portal.conecta.hub.module.classes.domain.model.Shift";
	private static final String TYPE_ROOM = "com.portal.conecta.hub.module.room.domain.model.TypeRoom";
	private static final String TYPE_USER = "com.portal.conecta.hub.module.user.domain.model.TypeUser";
	private static final String CLASS_MEMBERSHIP_ENTITY = "com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity";
	private static final String CLASS_MEMBERSHIP_ID = "com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId";
	private static final String USER_ENTITY = "com.portal.conecta.hub.module.user.domain.model.UserEntity";

	@Autowired
	private EntityManager entityManager;

	@Test
	void declaresIdentityAndAuditFieldsOnMainEntities() {
		assertEntityDeclaresFields(USER_ENTITY);
		assertEntityDeclaresFields(ROOM_ENTITY);
		assertEntityDeclaresFields(COURSE_ENTITY);
		assertEntityDeclaresFields(CLASS_ENTITY);
		assertDoesNotDeclareHibernateSoftDelete(USER_ENTITY);
		assertDoesNotDeclareHibernateSoftDelete(ROOM_ENTITY);
		assertDoesNotDeclareHibernateSoftDelete(COURSE_ENTITY);
		assertDoesNotDeclareHibernateSoftDelete(CLASS_ENTITY);
		assertDoesNotDeclareHibernateSoftDelete(CLASS_MEMBERSHIP_ENTITY);
	}

	@Test
	void persistsMainEntitiesWithAuditFieldsAndLazyRelationships() {
		Class<?> userClass = classFor(USER_ENTITY);
		Class<?> roomClass = classFor(ROOM_ENTITY);
		Class<?> courseClass = classFor(COURSE_ENTITY);
		Class<?> classClass = classFor(CLASS_ENTITY);
		Object creator = newUser("Ada Lovelace", "ada@portal.test", "ADMIN");
		Object room = newRoom(101, "CLASSROOM");
		Object course = newCourse("Robotica Educacional", "ROB-101");
		Object classEntity = newClassEntity("FULL_AM_PM", 1, "Robotica 1", course);

		invoke(room, "setCreatedBy", new Class<?>[] { userClass }, creator);
		invoke(course, "setCreatedBy", new Class<?>[] { userClass }, creator);
		invoke(classEntity, "setCreatedBy", new Class<?>[] { userClass }, creator);

		entityManager.persist(creator);
		entityManager.persist(room);
		entityManager.persist(course);
		entityManager.persist(classEntity);
		entityManager.flush();
		entityManager.clear();

		Object persistedUser = entityManager.find(userClass, idOf(creator));
		Object persistedRoom = entityManager.find(roomClass, idOf(room));
		Object persistedCourse = entityManager.find(courseClass, idOf(course));
		Object persistedClass = entityManager.find(classClass, idOf(classEntity));

		assertNotNull(call(persistedUser, "getId"));
		assertNotNull(call(persistedRoom, "getId"));
		assertNotNull(call(persistedCourse, "getId"));
		assertNotNull(call(persistedClass, "getId"));
		assertNotNull(call(persistedUser, "getCreatedAt"));
		assertNotNull(call(persistedUser, "getUpdatedAt"));
		assertNull(call(persistedUser, "getDeletedAt"));
		assertEquals(enumValue(TYPE_ROOM, "CLASSROOM"), call(persistedRoom, "getTypeRoom"));		assertFalse(Hibernate.isInitialized(call(persistedCourse, "getClasses")));

		entityManager.clear();
		persistedClass = entityManager.find(classClass, idOf(classEntity));
		Object courseProxy = call(persistedClass, "getCourse");
		assertFalse(Hibernate.isInitialized(courseProxy));
		assertEquals("Robotica Educacional", call(courseProxy, "getName"));
	}

	@Test
	void persistsClassMembershipAsJoinEntityWithCompositeIdAndRole() {
		Class<?> classMembershipEntityClass = classFor(CLASS_MEMBERSHIP_ENTITY);
		Object student = newUser("Grace Hopper", "grace@portal.test", "STUDENT");
		Object course = newCourse("Programacao", "PRG-101");
		Object classEntity = newClassEntity("FULL_PM_NT", 2, "Programacao 2", course);

		entityManager.persist(student);
		entityManager.persist(course);
		entityManager.persist(classEntity);
		entityManager.flush();

		Object enrollment = newClassMembership(student, classEntity, "REPRESENTATIVE");

		entityManager.persist(enrollment);
		entityManager.flush();
		entityManager.clear();

		Object id = newClassMembershipId(idOf(student), idOf(classEntity));
		Object persistedEnrollment = entityManager.find(classMembershipEntityClass, id);

		assertNotNull(persistedEnrollment);
		assertEquals(id, call(persistedEnrollment, "getId"));
		assertEquals(enumValue(CLASS_ROLE, "REPRESENTATIVE"), call(persistedEnrollment, "getClassRole"));
		assertNotNull(call(persistedEnrollment, "getCreatedAt"));
		assertFalse(Hibernate.isInitialized(call(persistedEnrollment, "getUser")));
		assertFalse(Hibernate.isInitialized(call(persistedEnrollment, "getClassEntity")));
	}

	@Test
	void persistsClassMembershipCreatedBeforeParentIdsAreGenerated() {
		Object student = newUser("Katherine Johnson", "katherine@portal.test", "STUDENT");
		Object course = newCourse("Matematica Aplicada", "MAT-101");
		Object classEntity = newClassEntity("FULL_AM_PM", 3, "Matematica 3", course);
		Object enrollment = newClassMembership(student, classEntity, "STUDENT");

		entityManager.persist(student);
		entityManager.persist(course);
		entityManager.persist(classEntity);
		entityManager.persist(enrollment);
		entityManager.flush();
		entityManager.clear();

		Object id = newClassMembershipId(idOf(student), idOf(classEntity));

		assertNotNull(entityManager.find(classFor(CLASS_MEMBERSHIP_ENTITY), id));
	}

	@Test
	void enforcesUniqueUserEmail() {
		entityManager.persist(newUser("Primeiro Usuario", "unique@portal.test", "ADMIN"));
		entityManager.flush();
		entityManager.clear();

		entityManager.persist(newUser("Segundo Usuario", "unique@portal.test", "STUDENT"));
		assertThrows(PersistenceException.class, () -> entityManager.flush());
	}

	@Test
	void enforcesUniqueCourseName() {
		entityManager.persist(newCourse("Data Science", "DS-101"));
		entityManager.flush();
		entityManager.clear();

		entityManager.persist(newCourse("Data Science", "DS-102"));
		assertThrows(PersistenceException.class, () -> entityManager.flush());
	}

	@Test
	void enforcesUniqueCourseCode() {
		entityManager.persist(newCourse("Data Science", "DS-101"));
		entityManager.flush();
		entityManager.clear();

		entityManager.persist(newCourse("Data Engineering", "DS-101"));
		assertThrows(PersistenceException.class, () -> entityManager.flush());
	}

	@Test
	void explicitDeleteMarksAuditFieldsWithoutChangingUpdatedAt() {
		Class<?> userClass = classFor(USER_ENTITY);
		Class<?> roomClass = classFor(ROOM_ENTITY);
		Class<?> courseClass = classFor(COURSE_ENTITY);
		Class<?> classClass = classFor(CLASS_ENTITY);
		Object deletedBy = newUser("Auditor", "auditor@portal.test", "ADMIN");
		Object user = newUser("Usuario Removido", "removed-user@portal.test", "STUDENT");
		Object room = newRoom(202, "CLASSROOM");
		Object course = newCourse("Arquitetura de Software", "ARQ-101");
		Object classEntity = newClassEntity("FULL_PM_NT", 4, "Arquitetura 4", course);

		entityManager.persist(deletedBy);
		entityManager.persist(user);
		entityManager.persist(room);
		entityManager.persist(course);
		entityManager.persist(classEntity);
		entityManager.flush();

		UUID userId = idOf(user);
		UUID roomId = idOf(room);
		UUID courseId = idOf(course);
		UUID classId = idOf(classEntity);

		entityManager.clear();

		Object persistedUser = entityManager.find(userClass, userId);
		Object persistedRoom = entityManager.find(roomClass, roomId);
		Object persistedCourse = entityManager.find(courseClass, courseId);
		Object persistedClass = entityManager.find(classClass, classId);
		Object userUpdatedAt = call(persistedUser, "getUpdatedAt");
		Object roomUpdatedAt = call(persistedRoom, "getUpdatedAt");
		Object courseUpdatedAt = call(persistedCourse, "getUpdatedAt");
		Object classUpdatedAt = call(persistedClass, "getUpdatedAt");

		invoke(persistedUser, "delete", new Class<?>[] { userClass }, deletedBy);
		invoke(persistedRoom, "delete", new Class<?>[] { userClass }, deletedBy);
		invoke(persistedCourse, "delete", new Class<?>[] { userClass }, deletedBy);
		entityManager.flush();
		entityManager.clear();

		Object deletedUser = entityManager.find(userClass, userId);
		Object deletedRoom = entityManager.find(roomClass, roomId);
		Object deletedCourse = entityManager.find(courseClass, courseId);
		Object deletedClass = entityManager.find(classClass, classId);

		assertDeleted(deletedUser, deletedBy, userUpdatedAt);
		assertDeleted(deletedRoom, deletedBy, roomUpdatedAt);
		assertDeleted(deletedCourse, deletedBy, courseUpdatedAt);
		assertDeleted(deletedClass, deletedBy, classUpdatedAt);
	}

	private static Object newUser(String name, String email, String typeUser) {
		return instantiate(
			USER_ENTITY,
			new Class<?>[] { String.class, String.class, String.class, classFor(TYPE_USER) },
			name,
			email,
			"hash",
			enumValue(TYPE_USER, typeUser)
		);
	}

	private static Object newRoom(Integer number, String typeRoom) {
		return instantiate(
			ROOM_ENTITY,
			new Class<?>[] { Integer.class, classFor(TYPE_ROOM) },
			number,
			enumValue(TYPE_ROOM, typeRoom)
		);
	}

	private static Object newCourse(String name, String code) {
		return instantiate(COURSE_ENTITY, new Class<?>[] { String.class, String.class }, name, code);
	}

	private static Object newClassEntity(String shift, Integer number, String name, Object course) {
		return instantiate(
			CLASS_ENTITY,
			new Class<?>[] { classFor(SHIFT), Integer.class, String.class, classFor(COURSE_ENTITY) },
			enumValue(SHIFT, shift),
			number,
			name,
			course
		);
	}

	private static Object newClassMembership(Object user, Object classEntity, String roleClass) {
		return instantiate(
			CLASS_MEMBERSHIP_ENTITY,
			new Class<?>[] { classFor(USER_ENTITY), classFor(CLASS_ENTITY), classFor(CLASS_ROLE) },
			user,
			classEntity,
			enumValue(CLASS_ROLE, roleClass)
		);
	}

	private static Object newClassMembershipId(UUID userId, UUID classId) {
		return instantiate(CLASS_MEMBERSHIP_ID, new Class<?>[] { UUID.class, UUID.class }, userId, classId);
	}

	private static void assertDeleted(Object entity, Object deletedBy, Object expectedUpdatedAt) {
		assertNotNull(call(entity, "getDeletedAt"));
		assertEquals(idOf(deletedBy), idOf(call(entity, "getDeletedBy")));
		assertEquals(expectedUpdatedAt, call(entity, "getUpdatedAt"));
	}

	private static UUID idOf(Object entity) {
		return (UUID) call(entity, "getId");
	}

	private static Object call(Object target, String methodName) {
		try {
			return target.getClass().getMethod(methodName).invoke(target);
		}
		catch (IllegalAccessException | NoSuchMethodException exception) {
			throw new IllegalStateException(exception);
		}
		catch (InvocationTargetException exception) {
			throw new IllegalStateException(exception.getCause());
		}
	}

	private static void invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
		try {
			target.getClass().getMethod(methodName, parameterTypes).invoke(target, args);
		}
		catch (IllegalAccessException | NoSuchMethodException exception) {
			throw new IllegalStateException(exception);
		}
		catch (InvocationTargetException exception) {
			throw new IllegalStateException(exception.getCause());
		}
	}

	private static Object instantiate(String className, Class<?>[] parameterTypes, Object... args) {
		try {
			Constructor<?> constructor = classFor(className).getConstructor(parameterTypes);
			return constructor.newInstance(args);
		}
		catch (IllegalAccessException | InstantiationException | NoSuchMethodException exception) {
			throw new IllegalStateException(exception);
		}
		catch (InvocationTargetException exception) {
			throw new IllegalStateException(exception.getCause());
		}
	}

	private static Class<?> classFor(String className) {
		try {
			return Class.forName(className);
		}
		catch (ClassNotFoundException exception) {
			throw new IllegalStateException(exception);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Enum<?> enumValue(String enumClassName, String name) {
		return Enum.valueOf((Class) classFor(enumClassName), name);
	}

	private static void assertEntityDeclaresFields(String className) {
		Class<?> entityClass = classFor(className);
		assertDoesNotThrow(() -> entityClass.getDeclaredField("id"));
		assertDoesNotThrow(() -> entityClass.getDeclaredField("createdAt"));
		assertDoesNotThrow(() -> entityClass.getDeclaredField("updatedAt"));
		assertDoesNotThrow(() -> entityClass.getDeclaredField("deletedAt"));
		assertDoesNotThrow(() -> entityClass.getDeclaredField("createdBy"));
		assertDoesNotThrow(() -> entityClass.getDeclaredField("updatedBy"));
		assertDoesNotThrow(() -> entityClass.getDeclaredField("deletedBy"));
	}

	private static void assertDoesNotDeclareHibernateSoftDelete(String className) {
		Class<?> entityClass = classFor(className);
		for (var annotation : entityClass.getAnnotations()) {
			String annotationName = annotation.annotationType().getName();
			assertFalse(annotationName.equals("org.hibernate.annotations.SQLDelete"));
			assertFalse(annotationName.equals("org.hibernate.annotations.SQLRestriction"));
		}
	}
}
