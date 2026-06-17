package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

@Component
public class ClassMembershipValidator {

    private static final EnumSet<TypeUser> ALLOWED_EXECUTORS = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.SENAI
    );

    private static final  EnumSet<TypeUser> ALLOWED_TARGET_TYPES = EnumSet.of(
            TypeUser.STUDENT,
            TypeUser.TEACHER
    );

    public void validateExecutorCanAddMember(TypeUser executorType, UUID executorId, UUID targetUserId, ClassRole classRole) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw  new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem associar membros a uma turma.");
        }

        if (classRole == ClassRole.REPRESENTATIVE) {
            throw new ClassMembershipException("O papel REPRESENTATIVE não é permitido neste endpoint.");
        }

        if(executorId.equals(targetUserId)) {
            throw new ClassMembershipException("O usuário não pode se associar a uma turma.");
        }
    }

    public void validateTargetUserCanBeAdded (UserEntity targetUser, ClassRole classRole) {
        if (!targetUser.isActive() || targetUser.getDeletedAt() != null) {
            throw new ClassMembershipException("Usuário está inativo ou excluído.");
        }
        if (!ALLOWED_TARGET_TYPES.contains(targetUser.getTypeUser())) {
            throw new ClassMembershipException("O tipo de usuário " + targetUser.getTypeUser() + " não pode ser associado a uma turma por este endpoint.");
        }
        boolean valid = (targetUser.getTypeUser() == TypeUser.STUDENT && classRole == ClassRole.STUDENT)
                || (targetUser.getTypeUser() == TypeUser.TEACHER && classRole == ClassRole.TEACHER);
        if (!valid) {
            throw new ClassMembershipException(
                    "TypeUser " + targetUser.getTypeUser() + " não pode ser associado ao papel " + classRole + "."
            );
        }
    }

    public void validateNoDuplicateMembership (boolean alreadyExists) {
        if (alreadyExists) {
            throw new ClassMembershipException("O usuário já possui uma matrícula ativa nesta turma.");
        }
    }

    public void validateStudentClassLimit (ClassRole classRole, Long existingCount) {
        if (classRole == ClassRole.STUDENT && existingCount > 0) {
            throw new ClassMembershipException("O aluno já possui uma turma ativa.");
        }
    }

    public void validateExecutorCanPromote(TypeUser executorType) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem promover membros a representante.");
        }
    }

    public void validateTargetUserForPromotion(UserEntity targetUser, ClassMembershipEntity membership) {
        if (!targetUser.isActive() || targetUser.getDeletedAt() != null) {
            throw new ClassMembershipException("Usuário está inativo ou excluído.");
        }
        if (targetUser.getTypeUser() != TypeUser.STUDENT) {
            throw new ClassMembershipException("Apenas usuários com TypeUser STUDENT podem ser promovidos a REPRESENTATIVE.");
        }
        if (membership.getClassRole() != ClassRole.STUDENT) {
            throw new ClassMembershipException("Apenas matrículas com o papel STUDENT podem ser promovidas a REPRESENTATIVE.");
        }
    }

    public void validateRepresentativeSlotAvailable (long currentCount) {
        if (currentCount >= 2) {
            throw new ClassMembershipException("A turma já atingiu o número máximo de representantes ativos.");
        }
    }

    public void validateExecutorCanDemote(TypeUser executorType) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem remover um representante.");
        }
    }

    public void validateTargetUserForDemotion(ClassMembershipEntity membership) {
        if (!membership.isActive()) {
            throw new ClassMembershipException("Usuário ou turma está inativo ou excluído.");
        }
        if (membership.getClassRole() != ClassRole.REPRESENTATIVE) {
            throw new ClassMembershipException("Apenas matrículas com o papel REPRESENTATIVE podem ser rebaixadas.");
        }
        if (membership.getUser().getTypeUser() != TypeUser.REPRESENTATIVE) {
            throw new ClassMembershipException("Apenas usuários com TypeUser REPRESENTATIVE podem ser rebaixados.");
        }
    }

    public void validateExecutorCanDeleteMembership(TypeUser executorType, UUID executorId, UUID targetUserId) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem remover matrículas da turma.");
        }
        if (executorId.equals(targetUserId)) {
            throw new ClassMembershipException("O usuário não pode remover a própria matrícula.");
        }
    }
}
