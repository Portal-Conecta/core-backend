package com.portal.conecta.hub.module.user.domain.model;

/**
 * Tipos de usuário do Hub Core.
 *
 * <p>O tipo determina permissões e regras de negócio em todo o sistema:
 * <ul>
 *   <li>{@code STUDENT} — aluno vinculado a turmas;</li>
 *   <li>{@code REPRESENTATIVE} — aluno com papel de representante de turma;</li>
 *   <li>{@code TEACHER} — docente responsável por turmas;</li>
 *   <li>{@code SENAI} — colaborador institucional do SENAI;</li>
 *   <li>{@code WEG} — colaborador da empresa parceira WEG;</li>
 *   <li>{@code ADMIN} — administrador com acesso irrestrito.</li>
 * </ul>
 *
 * <p>Usado como authority no Spring Security no formato {@code ROLE_<tipo>}
 * e persistido como {@code String} na coluna {@code type_user} da tabela {@code users}.
 */
public enum TypeUser {
	STUDENT,
	REPRESENTATIVE,
	TEACHER,
	SENAI,
	WEG,
	ADMIN
}
