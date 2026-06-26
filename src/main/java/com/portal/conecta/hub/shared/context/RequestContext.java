package com.portal.conecta.hub.shared.context;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;

import java.util.List;
import java.util.UUID;

/**
 * Representa o contexto do usuário autenticado na requisição atual.
 *
 * <p>Carregado pelo filtro de autenticação JWT e disponibilizado via
 * {@link RequestContextProvider}. Contém identidade, tipo e vínculos
 * acadêmicos do usuário para uso nos use cases sem nova consulta ao banco.
 */
public record RequestContext (
        UUID userId,
        TypeUser userType,
        List<ContextClass> classes
) {
}
