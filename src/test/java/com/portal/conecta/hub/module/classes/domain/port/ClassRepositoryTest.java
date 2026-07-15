package com.portal.conecta.hub.module.classes.domain.port;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClassRepositoryTest {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("deve atualizar nomes das turmas em lote quando codigo do curso muda")
    void shouldBulkUpdateClassNamesWhenCourseCodeChanges() {
        UserEntity updatedBy = new UserEntity("User Test", "user@test.com", "hash", TypeUser.SENAI);
        CourseEntity course = CourseEntity.create("Desenvolvimento de Sistemas", "DS");
        ClassEntity firstClass = ClassEntity.create(Shift.FULL_AM_PM, 1, course, updatedBy);
        ClassEntity secondClass = ClassEntity.create(Shift.FULL_PM_NT, 2, course, updatedBy);

        entityManager.persist(updatedBy);
        entityManager.persist(course);
        entityManager.persist(firstClass);
        entityManager.persist(secondClass);
        entityManager.flush();

        UUID courseId = course.getId();
        UUID firstClassId = firstClass.getId();
        UUID secondClassId = secondClass.getId();
        UUID updatedById = updatedBy.getId();

        int updatedClasses = classRepository.updateNamesByCourseId(courseId, "DEV", updatedById);
        entityManager.clear();

        assertThat(updatedClasses).isEqualTo(2);
        assertThat(entityManager.find(ClassEntity.class, firstClassId).getName()).isEqualTo("DEV1");
        assertThat(entityManager.find(ClassEntity.class, secondClassId).getName()).isEqualTo("DEV2");
    }
}
