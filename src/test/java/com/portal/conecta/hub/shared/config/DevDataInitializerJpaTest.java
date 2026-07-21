package com.portal.conecta.hub.shared.config;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@ActiveProfiles("test")
class DevDataInitializerJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassMembershipRepository membershipRepository;

    private final DevDataInitializer initializer = new DevDataInitializer();
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void shouldPersistManualIdsForCatalogRooms() {
        initializer.seedActiveRooms(roomRepository, null);
        entityManager.clear();

        assertRoomId(101, "00000000-0000-0000-0000-000000000101");
        assertRoomId(214, "00000000-0000-0000-0000-000000000214");
    }

    @Test
    void shouldSeedPresentationCoursesClassesAndMemberships() {
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        initializer.seedPresentationData(
                userRepository,
                courseRepository,
                classRepository,
                membershipRepository,
                roomRepository,
                passwordEncoder
        );
        entityManager.flush();
        entityManager.clear();

        assertEquals(Set.of("MI", "MT", "WU", "ME", "MA", "MM", "WME", "WQ", "MF", "MQ", "WM", "WA"),
                courseRepository.findAllByDeletedAtIsNull().stream().map(course -> course.getCode()).collect(java.util.stream.Collectors.toSet()));
        assertEquals(20, classRepository.count());

        ClassEntity mi77 = classByName("MI77");
        ClassEntity mi78 = classByName("MI78");
        assertEquals(4, membershipRepository.countByClassIdAndClassRole(mi77.getId(), ClassRole.TEACHER));
        assertEquals(4, membershipRepository.countByClassIdAndClassRole(mi78.getId(), ClassRole.TEACHER));
        assertEquals(2, membershipRepository.countByClassIdAndClassRole(mi78.getId(), ClassRole.REPRESENTATIVE));
        assertEquals(17, membershipRepository.countByClassIdAndClassRole(mi78.getId(), ClassRole.STUDENT));

        classRepository.findAll().stream()
                .filter(classEntity -> !classEntity.getName().equals("MI77") && !classEntity.getName().equals("MI78"))
                .forEach(this::assertDemoMembers);

        assertTrue(userRepository.existsByEmailIgnoreCase("lucas_eckert@estudante.sesisenai.org.br"));
        assertTrue(userRepository.existsByEmailIgnoreCase("leticia_guths@estudante.sesisenai.org.br"));
        assertTrue(userRepository.existsByEmailIgnoreCase("bruna.meinerz@sc.senai.br"));
        assertTrue(userRepository.existsByEmailIgnoreCase("nathalya@weg.net"));
    }

    private void assertRoomId(int number, String expectedId) {
        RoomEntity room = roomRepository.findAll().stream()
                .filter(candidate -> candidate.getNumber().equals(number))
                .findFirst()
                .orElseThrow();

        assertEquals(UUID.fromString(expectedId), room.getId());
    }

    private ClassEntity classByName(String name) {
        return classRepository.findAll().stream()
                .filter(classEntity -> classEntity.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private void assertDemoMembers(ClassEntity classEntity) {
        assertEquals(2, membershipRepository.countByClassIdAndClassRole(classEntity.getId(), ClassRole.REPRESENTATIVE));
        assertEquals(1, membershipRepository.countByClassIdAndClassRole(classEntity.getId(), ClassRole.STUDENT));
        assertEquals(0, membershipRepository.countByClassIdAndClassRole(classEntity.getId(), ClassRole.TEACHER));
    }
}
