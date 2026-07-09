package com.portal.conecta.hub.module.course.domain.validator;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Validador de domínio que centraliza as regras de autorização para manipulação de cursos.
 * <p>
 * O catálogo de cursos é central e gerido apenas por usuários corporativos
 * ou administradores de sistema (ex: perfis ADMIN, WEG, SENAI). Alunos ou perfis
 * genéricos não possuem acesso de escrita.
 */
@Component
public class CoursePermissionValidator {

    private static final EnumSet<TypeUser> PERMISSION_TYPES = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.SENAI
    );

    public boolean canCreate(TypeUser type) {
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }

    public boolean canUpdate(TypeUser type) {
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }

}