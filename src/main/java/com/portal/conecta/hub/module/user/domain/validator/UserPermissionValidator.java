package com.portal.conecta.hub.module.user.domain.validator;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Valida permissões de operações sobre usuários com base no tipo do requisitante.
 *
 * <p>Regras consolidadas:
 * <ul>
 *   <li>{@code ADMIN} pode criar, editar, desativar e listar qualquer usuário.</li>
 *   <li>{@code SENAI} pode criar {@code STUDENT} e {@code TEACHER}; editar e desativar
 *       {@code STUDENT}, {@code REPRESENTATIVE} e {@code TEACHER}.</li>
 *   <li>{@code WEG} pode criar {@code STUDENT} e {@code WEG}; editar e desativar
 *       {@code STUDENT} e {@code REPRESENTATIVE}.</li>
 *   <li>Somente {@code ADMIN} pode editar o próprio perfil.</li>
 *   <li>Apenas {@code ADMIN}, {@code SENAI} e {@code WEG} podem listar usuários.</li>
 * </ul>
 */
@Component
public class UserPermissionValidator {

    private static final Set<TypeUser> EMPTY = EnumSet.noneOf(TypeUser.class);

    private static final Map<TypeUser, Set<TypeUser>> CREATE_PERMISSIONS = Map.of(
            TypeUser.SENAI, EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER),
            TypeUser.WEG, EnumSet.of(TypeUser.STUDENT)
    );

    private static final Map<TypeUser, Set<TypeUser>> DEACTIVATE_PERMISSIONS = Map.of(
            TypeUser.SENAI, EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER, TypeUser.REPRESENTATIVE),
            TypeUser.WEG, EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE)
    );

    private static final Map<TypeUser, Set<TypeUser>> EDIT_PERMISSIONS = Map.of(
        TypeUser.SENAI, EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE, TypeUser.TEACHER),
        TypeUser.WEG, EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE)
    );

    /**
     * Verifica se o requisitante pode criar um usuário do tipo alvo.
     *
     * @param requester tipo do usuário que solicita a criação.
     * @param target    tipo do usuário a ser criado.
     * @return {@code true} se a criação for permitida.
     */
    public boolean canCreate(TypeUser requester, TypeUser target) {
        if (requester == null || target == null) {
            return false;
        }

        if (requester == TypeUser.ADMIN) {
            return true;
        }

        return CREATE_PERMISSIONS.getOrDefault(requester, EMPTY).contains(target);
    }

    /**
     * Verifica se o requisitante pode editar o usuário alvo.
     *
     * <p>{@code ADMIN} pode editar qualquer usuário, inclusive a si próprio.
     * Os demais perfis não podem realizar autoedição e só podem editar os
     * tipos previstos na matriz de permissões.
     *
     * @param requesterId   ID do usuário que solicita a edição.
     * @param requesterType tipo do usuário que solicita a edição.
     * @param targetId      ID do usuário a ser editado.
     * @param targetType    tipo do usuário a ser editado.
     * @return {@code true} se a edição for permitida.
     */
    public boolean canEdit(UUID requesterId, TypeUser requesterType, UUID targetId, TypeUser targetType) {
        if (requesterId == null || requesterType == null || targetId == null || targetType == null) {
            return false;
        }
        if (requesterType == TypeUser.ADMIN) {
            return true;
        }
        if (requesterId.equals(targetId)) {
            return false;
        }

        return EDIT_PERMISSIONS.getOrDefault(requesterType, EMPTY).contains(targetType);
    }


    /**
     * Valida se o requisitante pode editar o usuário alvo.
     *
     * @throws UserPermissionDeniedException se a edição não for permitida.
     */
    public void validateCanEdit(UUID requesterId, TypeUser requesterType, UUID targetId, TypeUser targetType){
        if (!canEdit(requesterId, requesterType, targetId, targetType)){
            throw new UserPermissionDeniedException("Usuário não tem permissão para editar este usuário.");
        }
    }

    /**
     * Valida se o requisitante pode criar um usuário do tipo alvo.
     *
     * @param requester tipo do usuário que solicita a criação.
     * @param target    tipo do usuário a ser criado.
     * @throws InvalidUserDataException      se {@code target} for nulo.
     * @throws UserPermissionDeniedException se a criação não for permitida.
     */
    public void validateCanCreate(TypeUser requester, TypeUser target) {
        if (target == null) {
            throw new InvalidUserDataException("Tipo de Usuário é obrigatório.");
        }

        if (!canCreate(requester, target)) {
            throw new UserPermissionDeniedException("Usuário não tem permissão para criar este tipo de usuário.");
        }
    }

    /**
     * Verifica se o requisitante pode desativar o usuário alvo.
     *
     * @param requester tipo do usuário que solicita a desativação.
     * @param target    tipo do usuário a ser desativado.
     * @return {@code true} se a desativação for permitida.
     */
    public boolean canDeactivate(TypeUser requester, TypeUser target) {
        if (requester == null || target == null) {
            return false;
        }

        if (requester == TypeUser.ADMIN) {
            return true;
        }

        return DEACTIVATE_PERMISSIONS.getOrDefault(requester, EMPTY).contains(target);
    }

    /**
     * Valida se o requisitante pode desativar o usuário alvo.
     *
     * @throws InvalidUserDataException      se {@code target} for nulo.
     * @throws UserPermissionDeniedException se a desativação não for permitida.
     */
    public void validateCanDeactivate(TypeUser requester, TypeUser target) {
        if (target == null) {
            throw new InvalidUserDataException("Tipo de Usuário é obrigatório.");
        }

        if (!canDeactivate(requester, target)) {
            throw new UserPermissionDeniedException("Usuário não tem permissão para desativar este tipo de usuário.");
        }
    }
}
