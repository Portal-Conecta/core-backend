package com.portal.conecta.hub.module.room.domain.validator;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Validador de domínio que centraliza as regras de autorização para operações em salas.
 * Restringe modificações de estado estrutural apenas a perfis administrativos ou institucionais.
 */
@Component
public class RoomPermissionValidator {

    private static final EnumSet<TypeUser> ALLOWED_TYPES = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.WEG
    );

    /**
     * Verifica se o perfil informado possui privilégios para criar novas salas.
     */
    public boolean canCreate(TypeUser typeUser) {
        if (typeUser == null) {
            return false;
        }
        return ALLOWED_TYPES.contains(typeUser);
    }

    /**
     * Verifica se o perfil informado possui privilégios para alterar dados de uma sala existente.
     */
    public boolean canUpdate(TypeUser typeUser) {
        if (typeUser == null) {
            return false;
        }
        return ALLOWED_TYPES.contains(typeUser);
    }

    /**
     * Verifica se o perfil informado possui privilégios para executar a remoção lógica da sala.
     */
    public boolean canRemove (TypeUser typeUser){
        if (typeUser == null){
            return false;
        }
        return ALLOWED_TYPES.contains(typeUser);
    }

    /**
     * Verifica se o perfil informado possui privilégios para reverter a exclusão lógica de uma sala.
     */
    public boolean canRestore(TypeUser typeUser) {
        if (typeUser == null) {
            return false;
        }
        return ALLOWED_TYPES.contains(typeUser);
    }
}
