package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import java.util.EnumSet;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Validador das regras de negocio de matriculas e papeis em turmas.
 *
 * <p>Centraliza permissoes de executor, compatibilidade entre tipo global de
 * usuario e papel na turma, limites de estudante/representante e elegibilidade
 * por {@link AccountStatus}.</p>
 */
@Component
public class ClassMembershipValidator {

    private static final EnumSet<TypeUser> ALLOWED_EXECUTORS = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.SENAI
    );

    private static final EnumSet<TypeUser> ALLOWED_TARGET_TYPES = EnumSet.of(
            TypeUser.STUDENT,
            TypeUser.TEACHER
    );

    private static final EnumSet<AccountStatus> ADDABLE_ACCOUNT_STATUSES = EnumSet.of(
            AccountStatus.ACTIVE,
            AccountStatus.PENDING_ACTIVATION
    );

    public void validateExecutorCanAddMember(TypeUser executorType, UUID executorId, UUID targetUserId, ClassRole classRole) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem associar membros a uma turma.");
        }

        if (classRole == ClassRole.REPRESENTATIVE) {
            throw new ClassMembershipException("O papel REPRESENTATIVE nao e permitido neste endpoint.");
        }

        if (executorId.equals(targetUserId)) {
            throw new ClassMembershipException("O usuario nao pode se associar a uma turma.");
        }
    }

    public void validateTargetUserCanBeAdded(UserEntity targetUser, ClassRole classRole) {
        if (!ADDABLE_ACCOUNT_STATUSES.contains(targetUser.getAccountStatus())) {
            throw new ClassMembershipException("Usuario nao pode ser associado a uma turma neste estado.");
        }
        if (!ALLOWED_TARGET_TYPES.contains(targetUser.getTypeUser())) {
            throw new ClassMembershipException("O tipo de usuario " + targetUser.getTypeUser() + " nao pode ser associado a uma turma por este endpoint.");
        }
        boolean valid = (targetUser.getTypeUser() == TypeUser.STUDENT && classRole == ClassRole.STUDENT)
                || (targetUser.getTypeUser() == TypeUser.TEACHER && classRole == ClassRole.TEACHER);
        if (!valid) {
            throw new ClassMembershipException(
                    "TypeUser " + targetUser.getTypeUser() + " nao pode ser associado ao papel " + classRole + "."
            );
        }
    }

    public void validateNoDuplicateMembership(boolean alreadyExists) {
        if (alreadyExists) {
            throw new ClassMembershipException("O usuario ja possui uma matricula ativa nesta turma.");
        }
    }

    public void validateStudentClassLimit(ClassRole classRole, Long existingCount) {
        if (classRole == ClassRole.STUDENT && existingCount > 0) {
            throw new ClassMembershipException("O aluno ja possui uma turma ativa.");
        }
    }

    public void validateExecutorCanPromote(TypeUser executorType) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem promover membros a representante.");
        }
    }

    public void validateTargetUserForPromotion(UserEntity targetUser, ClassMembershipEntity membership) {
        if (targetUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ClassMembershipException("Usuario esta inativo ou excluido.");
        }
        if (targetUser.getTypeUser() != TypeUser.STUDENT) {
            throw new ClassMembershipException("Apenas usuarios com TypeUser STUDENT podem ser promovidos a REPRESENTATIVE.");
        }
        if (membership.getClassRole() != ClassRole.STUDENT) {
            throw new ClassMembershipException("Apenas matriculas com o papel STUDENT podem ser promovidas a REPRESENTATIVE.");
        }
    }

    public void validateRepresentativeSlotAvailable(long currentCount) {
        if (currentCount >= 2) {
            throw new ClassMembershipException("A turma ja atingiu o numero maximo de representantes ativos.");
        }
    }

    public void validateExecutorCanDemote(TypeUser executorType) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem remover um representante.");
        }
    }

    public void validateTargetUserForDemotion(ClassMembershipEntity membership) {
        if (!membership.isActive()) {
            throw new ClassMembershipException("Usuario ou turma esta inativo ou excluido.");
        }
        if (membership.getClassRole() != ClassRole.REPRESENTATIVE) {
            throw new ClassMembershipException("Apenas matriculas com o papel REPRESENTATIVE podem ser rebaixadas.");
        }
        if (membership.getUser().getTypeUser() != TypeUser.REPRESENTATIVE) {
            throw new ClassMembershipException("Apenas usuarios com TypeUser REPRESENTATIVE podem ser rebaixados.");
        }
    }

    public void validateExecutorCanDeleteMembership(TypeUser executorType, UUID executorId, UUID targetUserId) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem remover matriculas da turma.");
        }
        if (executorId.equals(targetUserId)) {
            throw new ClassMembershipException("O usuario nao pode remover a propria matricula.");
        }
    }
}
