package com.portal.conecta.hub.module.user.domain.specification;

import static org.assertj.core.api.Assertions.assertThat;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserSpecificationsIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void semTurmaAtivaExcludesOnlyAcademicUsersWithActiveAcademicMembership() {
        UserEntity executor = user("Executor", TypeUser.SENAI);
        UserEntity availableStudent = user("Aluno disponivel", TypeUser.STUDENT);
        UserEntity activeStudent = user("Aluno matriculado", TypeUser.STUDENT);
        UserEntity inactiveClassStudent = user("Aluno turma inativa", TypeUser.STUDENT);
        UserEntity removedMembershipStudent = user("Aluno vinculo removido", TypeUser.STUDENT);
        UserEntity representative = user("Representante matriculado", TypeUser.REPRESENTATIVE);
        UserEntity teacher = user("Professor matriculado", TypeUser.TEACHER);
        CourseEntity course = CourseEntity.create("Desenvolvimento de Sistemas", "DS");
        ClassEntity activeClass = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);
        ClassEntity inactiveClass = ClassEntity.create(Shift.FULL_PM_NT, 2, course, executor);
        inactiveClass.deactivate(executor);

        persist(executor, availableStudent, activeStudent, inactiveClassStudent, removedMembershipStudent, representative, teacher, course, activeClass, inactiveClass);
        ClassMembershipEntity removedMembership = new ClassMembershipEntity(removedMembershipStudent, activeClass, ClassRole.STUDENT);
        persist(
                new ClassMembershipEntity(activeStudent, activeClass, ClassRole.STUDENT),
                new ClassMembershipEntity(inactiveClassStudent, inactiveClass, ClassRole.STUDENT),
                removedMembership,
                new ClassMembershipEntity(representative, activeClass, ClassRole.REPRESENTATIVE),
                new ClassMembershipEntity(teacher, activeClass, ClassRole.TEACHER)
        );
        entityManager.flush();
        entityManager.remove(removedMembership);
        entityManager.flush();
        entityManager.clear();

        List<String> names = userRepository.findAll(
                        UserSpecifications.from(new GetAllUserQuery(0, 20, null, null, null, true)),
                        PageRequest.of(0, 20)
                )
                .map(UserEntity::getName)
                .getContent();

        assertThat(names)
                .contains("Aluno disponivel", "Aluno turma inativa", "Aluno vinculo removido", "Professor matriculado")
                .doesNotContain("Aluno matriculado", "Representante matriculado");
    }

    @Test
    void semTurmaAtivaFalsePreservesTheExistingStudentSearch() {
        UserEntity executor = user("Executor", TypeUser.SENAI);
        UserEntity enrolledStudent = user("Aluno matriculado", TypeUser.STUDENT);
        CourseEntity course = CourseEntity.create("Eletrotecnica", "ELT");
        ClassEntity activeClass = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);

        persist(executor, enrolledStudent, course, activeClass);
        persist(new ClassMembershipEntity(enrolledStudent, activeClass, ClassRole.STUDENT));
        entityManager.flush();
        entityManager.clear();

        List<String> names = userRepository.findAll(
                        UserSpecifications.from(new GetAllUserQuery(0, 20, TypeUser.STUDENT, "matriculado", null, false)),
                        PageRequest.of(0, 20)
                )
                .map(UserEntity::getName)
                .getContent();

        assertThat(names).containsExactly("Aluno matriculado");
    }

    private UserEntity user(String name, TypeUser type) {
        return new UserEntity(name, name.replace(' ', '.') + "@test.local", "hash", type);
    }

    private void persist(Object... entities) {
        for (Object entity : entities) {
            entityManager.persist(entity);
        }
    }
}
