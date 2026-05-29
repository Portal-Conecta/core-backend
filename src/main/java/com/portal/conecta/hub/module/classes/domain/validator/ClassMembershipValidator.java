package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
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
            throw  new UserPermissionDeniedException("Only ADMIN or SENAI can associate members to a class.");
        }

        if (classRole == ClassRole.REPRESENTATIVE) {
            throw new ClassMembershipException("REPRESENTATIVE role is not allowed in this endpoint.");
        }

        if(executorId.equals(targetUserId)) {
            throw new ClassMembershipException("User cannot associate themselves to a class.");
        }
    }

    public void validateClassIsActive(ClassEntity classEntity) {
        if (classEntity.getDeletedAt() != null) {
            throw new ClassMembershipException("Class is deleted and cannot receive new members.");
        }
    }

    public void validateTargetUserCanBeAdded (UserEntity targetUser, ClassRole classRole) {
        if (!targetUser.isActive() || targetUser.getDeletedAt() != null) {
            throw new ClassMembershipException("User is inactive or deleted.");
        }
        if (!ALLOWED_TARGET_TYPES.contains(targetUser.getTypeUser())) {
            throw new ClassMembershipException("User type " + targetUser.getTypeUser() + " cannot be associated to a class by this endpoint.");
        }
        boolean valid = (targetUser.getTypeUser() == TypeUser.STUDENT && classRole == ClassRole.STUDENT)
                || (targetUser.getTypeUser() == TypeUser.TEACHER && classRole == ClassRole.TEACHER);
        if (!valid) {
            throw new ClassMembershipException(
                    "TypeUser " + targetUser.getTypeUser() + " cannot be associated with role " + classRole + "."
            );
        }
    }

    public void validateNoDuplicateMembership (boolean alreadyExists) {
        if (alreadyExists) {
            throw new ClassMembershipException("User already has an active membership in this class.");
        }
    }

    public void validateStudentClassLimit (ClassRole classRole, Long existingCount) {
        if (classRole == ClassRole.STUDENT && existingCount > 0) {
            throw new ClassMembershipException("Student already has an active class.");
        }
    }

    public void validateExecutorCanPromote(TypeUser executorType) {
        if (!ALLOWED_EXECUTORS.contains(executorType)) {
            throw new UserPermissionDeniedException("Only ADMIN or SENAI can promote members to representative.");
        }
    }

    public void validateTargetUserForPromotion(UserEntity targetUser, ClassMembershipEntity membership) {
        if (!targetUser.isActive() || targetUser.getDeletedAt() != null) {
            throw new ClassMembershipException("User is inactive or deleted.");
        }
        if (targetUser.getTypeUser() != TypeUser.STUDENT) {
            throw new ClassMembershipException("Only users with TypeUser STUDENT can be promoted to REPRESENTATIVE.");
        }
        if (membership.getClassRole() != ClassRole.STUDENT) {
            throw new ClassMembershipException("Only memberships with role STUDENT can be promoted to REPRESENTATIVE.");
        }
    }

    public void validateRepresentativeSlotAvailable (long currentCount) {
        if (currentCount >= 2) {
            throw new ClassMembershipException("Class already has the maximum number of active representatives.");
        }
    }
}
