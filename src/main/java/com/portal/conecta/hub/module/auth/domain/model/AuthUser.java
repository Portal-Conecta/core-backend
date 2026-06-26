package com.portal.conecta.hub.module.auth.domain.model;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;

import java.util.UUID;

/**
 * Projeção de domínio de um usuário autenticável.
 *
 * <p>Interface utilizada pelo módulo {@code auth} para desacoplar o processo de
 * autenticação da entidade concreta de usuário. Qualquer implementação que exponha
 * estes atributos pode participar do fluxo de login e geração de tokens.</p>
 */
public interface AuthUser {
    UUID getId();

    /**
     * @return hash Bcrypt da senha, usado para validação via {@code PasswordEncoder}
     * sem armazenar ou comparar a senha em texto puro.
     */
    String getPasswordHash();

    /**
     * @return tipo do usuário, utilizado como claim {@code userType} no access token
     */
    TypeUser getType();

    /**
     * Indica se o usuário está apto a autenticar-se.
     *
     * <p>Verificado antes da emissão de qualquer token; retornar {@code false}
     * causa rejeição do fluxo de autenticação pelo use case.</p>
     *
     * @return {@code true} se ativo; {@code false} se inativo ou bloqueado
     */
    boolean isActive();
}