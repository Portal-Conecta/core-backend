package com.portal.conecta.hub.module.me.infrastructure.projection;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;

import java.util.UUID;

/**
 * Interface de Projeção do Spring Data JPA utilizada para mapear o resultado otimizado
 * da consulta de vínculos entre o usuário logado, seus cursos e suas respectivas turmas.
 * <p>
 * Evita o carregamento de entidades completas na memória, extraindo diretamente
 * as colunas necessárias para a montagem e agrupamento do DTO de resposta.
 * </p>
 */
public interface UserCourseClassProjection {
    UUID getCourseId();
    String getCourseName();
    String getCourseCode();
    UUID getClassId();
    String getClassName();
    Integer getClassNumber();
    Shift getClassShift();
    ClassRole getRole();
}
