package com.portal.conecta.hub.module.classes.domain.model;

/**
 * Papel exercido por um usuário dentro de uma turma.
 *
 * <ul>
 *   <li>{@code STUDENT} — aprendiz padrão da turma.</li>
 *   <li>{@code TEACHER} — docente responsável pela turma.</li>
 *   <li>{@code REPRESENTATIVE} — aprendiz com status de representante;
 *       ocupa posição no mapa de sala e possui permissões adicionais.</li>
 * </ul>
 */
public enum ClassRole {
	STUDENT,
	TEACHER,
	REPRESENTATIVE
}
