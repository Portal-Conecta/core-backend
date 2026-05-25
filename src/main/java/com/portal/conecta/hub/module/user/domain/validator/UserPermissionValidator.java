package com.portal.conecta.hub.module.user.domain.validator;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class UserPermissionValidator {

    private static final Set<TypeUser> EMPTY = EnumSet.noneOf(TypeUser.class);

    private static final Map<TypeUser, Set<TypeUser>> CREATE_PERMISSIONS = Map.of(
            TypeUser.SENAI, EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER),
            TypeUser.WEG, EnumSet.of(TypeUser.STUDENT, TypeUser.WEG)
    );

    private static final Map<TypeUser, Set<TypeUser>> DEACTIVATE_PERMISSIONS = Map.of(
            TypeUser.SENAI, EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER, TypeUser.REPRESENTATIVE),
            TypeUser.WEG, EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE)
    );

    private static final Map<TypeUser, Set<TypeUser>> EDIT_PERMISSIONS = Map.of(
        TypeUser.SENAI, EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE, TypeUser.TEACHER),
        TypeUser.WEG, EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE)
    );


    private static final Set<TypeUser> LIST_USERS_PERMISSIONS = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.SENAI,
            TypeUser.WEG
    );

    public boolean canCreate(TypeUser requester, TypeUser target) {
        if (requester == null || target == null) {
            return false;
        }

        if (requester == TypeUser.ADMIN) {
            return true;
        }

        return CREATE_PERMISSIONS.getOrDefault(requester, EMPTY).contains(target);
    }

    public boolean canEdit (UUID requesterId, TypeUser requesterType, UUID targetId, TypeUser targetType){
        if (requesterType == null || targetType == null){
            return false;
        }
        if (requesterType == TypeUser.ADMIN){
            return true;
        }
        if (requesterId.equals(targetId)){
            return true;
        }

        return EDIT_PERMISSIONS.getOrDefault(requesterType, EMPTY).contains(targetType);
    }

    public void validateCanEdit(UUID requesterId, TypeUser requesterType, UUID targetId, TypeUser targetType){
        if (!canEdit(requesterId, requesterType, targetId, targetType)){
            throw new UserPermissionDeniedException("User does not have permission to edit this user");
        }
    }

    public void validateCanCreate(TypeUser requester, TypeUser target) {
        if (target == null) {
            throw new InvalidUserDataException("typeUser is required.");
        }

        if (!canCreate(requester, target)) {
            throw new UserPermissionDeniedException("User does not have permission to create this type of user.");
        }
    }

    public boolean canDeactivate(TypeUser requester, TypeUser target) {
        if (requester == null || target == null) {
            return false;
        }

        if (requester == TypeUser.ADMIN) {
            return true;
        }

        return DEACTIVATE_PERMISSIONS.getOrDefault(requester, EMPTY).contains(target);
    }

    public void validateCanDeactivate(TypeUser requester, TypeUser target) {
        if (target == null) {
            throw new InvalidUserDataException("typeUser is required.");
        }

        if (!canDeactivate(requester, target)) {
            throw new UserPermissionDeniedException("User does not have permission to deactivate this type of user.");
        }
    }

    public boolean canListUsers(TypeUser requester) {
        if (requester == null) {
            return false;
        }

        return LIST_USERS_PERMISSIONS.contains(requester);
    }

    public void validateCanListUsers(TypeUser requester) {
        if (!canListUsers(requester)) {
            throw new UserPermissionDeniedException("User does not have permission to list users.");
        }
    }
}