package com.portal.conecta.hub.shared.context;

/**
 * Contrato para obtenção do contexto do usuário autenticado na requisição corrente.
 *
 * <p>Use cases e componentes que precisam da identidade ou dos vínculos do usuário
 * devem depender desta interface, não do {@link org.springframework.security.core.context.SecurityContextHolder} diretamente.
 *
 * @see SecurityRequestContextProvider
 */
public interface RequestContextProvider {

    /**
     * Retorna o contexto do usuário autenticado.
     *
     * @throws com.portal.conecta.hub.shared.exception.UnauthorizedUserException se não houver autenticação válida na requisição corrente.
     */
    RequestContext getRequestContext();

}
