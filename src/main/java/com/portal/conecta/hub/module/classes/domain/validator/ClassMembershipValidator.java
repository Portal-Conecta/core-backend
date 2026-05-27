package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

@Component
public class ClassMembershipValidator {

    private static final EnumSet<TypeUser> ALlOWED_EXECUTORS = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.SENAI
    );

    private static final  EnumSet<TypeUser> ALLOWED_TARGET_TYPES = EnumSet.of(
            TypeUser.STUDENT,
            TypeUser.TEACHER
    );

    public void validateExecutorType(TypeUser executorType) {
        if (!ALlOWED_EXECUTORS.contains(executorType)) {
            throw  new UserPermissionDeniedException("Only ADMIN or SENAI can associate members to a class.");
        }
    }

    public void validateNoSelfAssociation(UUID executorId, UUID targetUserId) {
        if (executorId.equals(targetUserId)) {
            throw new ClassMembershipException("User cannot associate themselves to a class.");
        }
    }

    public void validateClassRoleNotRepresentative(ClassRole classRole) {
        if (classRole == ClassRole.REPRESENTATIVE) {
            throw new ClassMembershipException("REPRESENTATIVE role is not allowed in this endpoint.");
        }
    }

    public void validateTargetUserType(TypeUser targetType) {
        if (!ALLOWED_TARGET_TYPES.contains(targetType)) {
            throw new ClassMembershipException("User type " + targetType + " cannot be associated to a class by this endpoint.");
        }
    }

    public void validateTypeAndRoleCombination (TypeUser targetType, ClassRole classRole) {
        boolean valid = (targetType == TypeUser.STUDENT && classRole == ClassRole.STUDENT)
                || (targetType == TypeUser.TEACHER && classRole == ClassRole.TEACHER);
        if (!valid) {
            throw new ClassMembershipException(
                    "TypeUser " + targetType + " cannot be associated with role " + classRole + "."
            );
        }
    }

    public void validateNoDuplicateMembership (boolean alredyExists) {
        if (alredyExists) {
            throw new ClassMembershipException("User already has an active membership in this class.");
        }
    }

    public void validateStudentClassLimit (ClassRole classRole, Long existingCount) {
        if (classRole == ClassRole.STUDENT && existingCount > 0) {
            throw new ClassMembershipException("Student already has an active class.");
        }
    }

}
