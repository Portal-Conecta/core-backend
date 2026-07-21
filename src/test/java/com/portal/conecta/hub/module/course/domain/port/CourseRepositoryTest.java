package com.portal.conecta.hub.module.course.domain.port;

import static org.assertj.core.api.Assertions.assertThat;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsCourseByCodeOnlyWhenItIsNotDeleted() {
        UserEntity executor = new UserEntity("Executor", "executor@test.com", "hash", TypeUser.SENAI);
        CourseEntity availableCourse = CourseEntity.create("Desenvolvimento de Sistemas", "DEV-01");
        CourseEntity deletedCourse = CourseEntity.create("Eletrotécnica", "ELE-01");

        entityManager.persist(executor);
        entityManager.persist(availableCourse);
        entityManager.persist(deletedCourse);
        entityManager.flush();

        deletedCourse.delete(executor);
        entityManager.flush();
        entityManager.clear();

        assertThat(courseRepository.findByCodeAndDeletedAtIsNull("DEV-01")).isPresent();
        assertThat(courseRepository.findByCodeAndDeletedAtIsNull("ELE-01")).isEmpty();
    }
}
