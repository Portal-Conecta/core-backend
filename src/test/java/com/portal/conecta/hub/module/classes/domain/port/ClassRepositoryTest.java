package com.portal.conecta.hub.module.classes.domain.port;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClassRepositoryTest {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassMembershipRepository classMembershipRepository;

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

    @Test
    @DisplayName("deve retornar apenas vinculos de turmas ativas para o contexto do usuario")
    void shouldReturnOnlyActiveClassMembershipsForUserContext() {
        UserEntity executor = new UserEntity("Executor", "executor@test.com", "hash", TypeUser.SENAI);
        UserEntity student = new UserEntity("Aluno", "aluno@test.com", "hash", TypeUser.STUDENT);
        CourseEntity course = CourseEntity.create("Desenvolvimento de Sistemas", "DS");
        ClassEntity activeClass = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);
        ClassEntity inactiveClass = ClassEntity.create(Shift.FULL_PM_NT, 2, course, executor);
        ClassMembershipEntity activeMembership = new ClassMembershipEntity(student, activeClass, ClassRole.STUDENT);
        ClassMembershipEntity inactiveMembership = new ClassMembershipEntity(student, inactiveClass, ClassRole.STUDENT);

        inactiveClass.deactivate(executor);

        entityManager.persist(executor);
        entityManager.persist(student);
        entityManager.persist(course);
        entityManager.persist(activeClass);
        entityManager.persist(inactiveClass);
        entityManager.persist(activeMembership);
        entityManager.persist(inactiveMembership);
        entityManager.flush();
        entityManager.clear();

        assertThat(classMembershipRepository.findActiveByUserId(student.getId()))
                .extracting(membership -> membership.getClassEntity().getId())
                .containsExactly(activeClass.getId());
    }

    @Test
    @DisplayName("deve retornar membros desativados e pendentes, mas nao removidos")
    void shouldReturnDisabledAndPendingMembers() {
        UserEntity executor = new UserEntity("Executor", "executor@test.com", "hash", TypeUser.SENAI);
        UserEntity activeUser = new UserEntity("Ativo", "ativo@test.com", "hash", TypeUser.STUDENT);
        UserEntity disabledUser = new UserEntity("Desativado", "desativado@test.com", "hash", TypeUser.STUDENT);
        UserEntity pendingUser = UserEntity.createPendingActivation(
                "Pendente", "pendente@test.com", "hash", TypeUser.STUDENT, executor
        );
        UserEntity removedUser = new UserEntity("Removido", "removido@test.com", "hash", TypeUser.STUDENT);
        CourseEntity course = CourseEntity.create("Desenvolvimento de Sistemas", "DS");
        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);

        disabledUser.deactivate(executor);
        removedUser.delete(executor);

        entityManager.persist(executor);
        entityManager.persist(activeUser);
        entityManager.persist(disabledUser);
        entityManager.persist(pendingUser);
        entityManager.persist(removedUser);
        entityManager.persist(course);
        entityManager.persist(classEntity);
        entityManager.persist(new ClassMembershipEntity(activeUser, classEntity, ClassRole.STUDENT));
        entityManager.persist(new ClassMembershipEntity(disabledUser, classEntity, ClassRole.STUDENT));
        entityManager.persist(new ClassMembershipEntity(pendingUser, classEntity, ClassRole.STUDENT));
        entityManager.persist(new ClassMembershipEntity(removedUser, classEntity, ClassRole.STUDENT));
        entityManager.flush();
        entityManager.clear();

        assertThat(classMembershipRepository.findNonRemovedMembersByClassIdAndRoles(
                classEntity.getId(), EnumSet.of(ClassRole.STUDENT)
        )).extracting(membership -> membership.getUser().getAccountStatus())
                .containsExactlyInAnyOrder(
                        AccountStatus.ACTIVE,
                        AccountStatus.DISABLED,
                        AccountStatus.PENDING_ACTIVATION
                );
    }
}
