package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Valida permissões de operações sobre turmas com base no tipo do usuário autenticado.
 *
 * <p>Apenas usuários dos tipos {@code ADMIN}, {@code SENAI} e {@code WEG} têm
 * permissão para criar, excluir, restaurar, desativar e reativar turmas.</p>
 *
 * <p>Métodos prefixados com {@code can} retornam {@code boolean} para uso condicional.
 * Métodos prefixados com {@code validate} lançam {@link UserPermissionDeniedException}
 * diretamente, sendo adequados para uso nos use cases.</p>
 */
@Component
public class ClassPermissionValidator {

    private static final EnumSet<TypeUser> PERMISSION_TYPES = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.WEG,
            TypeUser.SENAI
    );

    /**
     * Verifica se o tipo de usuário pode criar turmas.
     *
     * @param type tipo do usuário autenticado; {@code null} retorna {@code false}.
     * @return {@code true} se permitido.
     */
    public boolean canCreate(TypeUser type) {
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }

    /**
     * Verifica se o tipo de usuário pode excluir turmas logicamente.
     *
     * @param type tipo do usuário autenticado; {@code null} retorna {@code false}.
     * @return {@code true} se permitido.
     */
    public boolean canDelete(TypeUser type){
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }

    /**
     * Valida permissão para exclusão lógica, lançando exceção se negada.
     *
     * @param type tipo do usuário autenticado.
     * @throws UserPermissionDeniedException se o tipo não tiver permissão.
     */
    public void validateCanDelete (TypeUser type){
        if (!canDelete(type)){
            throw new UserPermissionDeniedException("Usuário não tem permissão para excluir uma turma.");
        }
    }

    /**
     * Verifica se o tipo de usuário pode restaurar turmas removidas logicamente.
     *
     * @param type tipo do usuário autenticado; {@code null} retorna {@code false}.
     * @return {@code true} se permitido.
     */
    public boolean canRestore (TypeUser type){
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }

    /**
     * Valida permissão para restauração, lançando exceção se negada.
     *
     * @param type tipo do usuário autenticado.
     * @throws UserPermissionDeniedException se o tipo não tiver permissão.
     */
    public void validateCanRestore(TypeUser type){
        if (!canRestore(type)){
            throw new UserPermissionDeniedException("Usuário não tem permissão para restaurar uma turma.");
        }
    }

    /**
     * Verifica se o tipo de usuário pode desativar turmas.
     *
     * @param type tipo do usuário autenticado; {@code null} retorna {@code false}.
     * @return {@code true} se permitido.
     */
    public boolean canDeactivate(TypeUser type) {
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }

    /**
     * Valida permissão para desativação, lançando exceção se negada.
     *
     * @param type tipo do usuário autenticado.
     * @throws UserPermissionDeniedException se o tipo não tiver permissão.
     */
    public void validateCanDeactivate(TypeUser type) {
        if (!canDeactivate(type)) {
            throw new UserPermissionDeniedException("Usuário não tem permissão para inativar uma turma.");
        }
    }

    /**
     * Verifica se o tipo de usuário pode reativar turmas desativadas.
     *
     * @param type tipo do usuário autenticado; {@code null} retorna {@code false}.
     * @return {@code true} se permitido.
     */
    public boolean canReactivate(TypeUser type) {
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }

    /**
     * Valida permissão para reativação, lançando exceção se negada.
     *
     * @param type tipo do usuário autenticado.
     * @throws UserPermissionDeniedException se o tipo não tiver permissão.
     */
    public void validateCanReactivate(TypeUser type) {
        if (!canReactivate(type)) {
            throw new UserPermissionDeniedException("Usuário não tem permissão para reativar uma turma.");
        }
    }

}
