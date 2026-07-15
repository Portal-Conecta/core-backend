    package com.portal.conecta.hub.module.classes.domain.validator;

    import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
    import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
    import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
    import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
    import com.portal.conecta.hub.module.user.domain.model.TypeUser;
    import com.portal.conecta.hub.module.user.domain.model.UserEntity;
    import org.springframework.stereotype.Component;

    import java.util.EnumSet;
    import java.util.UUID;

    /**
     * Valida regras de negócio para operações de vínculo de membros em turmas.
     *
     * <p>Apenas {@code ADMIN} e {@code SENAI} podem executar operações de membership.
     * Apenas usuários com {@code TypeUser} {@code STUDENT} ou {@code TEACHER} podem
     * ser vinculados a turmas — e o papel ({@link ClassRole}) deve ser compatível
     * com o tipo do usuário-alvo.</p>
     */
    @Component
    public class ClassMembershipValidator {

        private static final EnumSet<TypeUser> ALLOWED_EXECUTORS = EnumSet.of(
                TypeUser.ADMIN,
                TypeUser.SENAI
        );

        private static final  EnumSet<TypeUser> ALLOWED_TARGET_TYPES = EnumSet.of(
                TypeUser.STUDENT,
                TypeUser.TEACHER
        );

        /**
         * Valida se o executor pode adicionar um membro à turma.
         *
         * <p>Impede: executor sem permissão, papel {@code REPRESENTATIVE} via este fluxo
         * e auto-associação (executor tentando se vincular à própria turma).</p>
         *
         * @param executorType  tipo do usuário que executa a operação.
         * @param executorId    ID do executor.
         * @param targetUserId  ID do usuário a ser vinculado.
         * @param classRole     papel solicitado para o novo vínculo.
         * @throws UserPermissionDeniedException se o executor não for {@code ADMIN} ou {@code SENAI}.
         * @throws ClassMembershipException      se o papel for {@code REPRESENTATIVE} ou executor e alvo forem o mesmo usuário.
         */
        public void validateExecutorCanAddMember(TypeUser executorType, UUID executorId, UUID targetUserId, ClassRole classRole) {
            if (!ALLOWED_EXECUTORS.contains(executorType)) {
                throw  new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem associar membros a uma turma.");
            }

            if (classRole == ClassRole.REPRESENTATIVE) {
                throw new ClassMembershipException("O papel REPRESENTATIVE não é permitido neste endpoint.");
            }

            if(executorId.equals(targetUserId)) {
                throw new ClassMembershipException("O usuário não pode se associar a uma turma.");
            }
        }

        /**
         * Valida se o usuário-alvo pode ser vinculado à turma com o papel informado.
         *
         * <p>Rejeita usuários inativos, removidos, com tipo não permitido
         * ou com incompatibilidade entre {@code TypeUser} e {@code ClassRole}:
         * {@code STUDENT} aceita apenas {@code STUDENT}; {@code TEACHER} aceita apenas {@code TEACHER}.</p>
         *
         * @param targetUser usuário a ser vinculado.
         * @param classRole  papel solicitado.
         * @throws ClassMembershipException se o usuário não for elegível.
         */
        public void validateTargetUserCanBeAdded (UserEntity targetUser, ClassRole classRole) {
            if (targetUser.isRemoved()) {
                throw new ClassMembershipException("Usuário está excluído.");
            }
            if (!ALLOWED_TARGET_TYPES.contains(targetUser.getTypeUser())) {
                throw new ClassMembershipException("O tipo de usuário " + targetUser.getTypeUser() + " não pode ser associado a uma turma por este endpoint.");
            }
            boolean valid = (targetUser.getTypeUser() == TypeUser.STUDENT && classRole == ClassRole.STUDENT)
                    || (targetUser.getTypeUser() == TypeUser.TEACHER && classRole == ClassRole.TEACHER);
            if (!valid) {
                throw new ClassMembershipException(
                        "TypeUser " + targetUser.getTypeUser() + " não pode ser associado ao papel " + classRole + "."
                );
            }
        }

        /**
         * Valida que não existe vínculo duplicado para o usuário na turma.
         *
         * @param alreadyExists resultado da verificação de duplicidade no repositório.
         * @throws ClassMembershipException se já existir vínculo ativo.
         */
        public void validateNoDuplicateMembership (boolean alreadyExists) {
            if (alreadyExists) {
                throw new ClassMembershipException("O usuário já possui uma matrícula ativa nesta turma.");
            }
        }

        /**
         * Valida o limite de turmas simultâneas para estudantes.
         *
         * <p>Um estudante pode estar vinculado a no máximo uma turma ativa.</p>
         *
         * @param classRole     papel do vínculo sendo criado.
         * @param existingCount quantidade de turmas ativas com esse papel para o usuário.
         * @throws ClassMembershipException se o estudante já possuir uma turma ativa.
         */
        public void validateStudentClassLimit (ClassRole classRole, Long existingCount) {
            if (classRole == ClassRole.STUDENT && existingCount > 0) {
                throw new ClassMembershipException("O aluno já possui uma turma ativa.");
            }
        }

        /**
         * Valida se o executor pode promover um membro a representante.
         *
         * @param executorType tipo do usuário que executa a operação.
         * @throws UserPermissionDeniedException se o executor não for {@code ADMIN} ou {@code SENAI}.
         */
        public void validateExecutorCanPromote(TypeUser executorType) {
            if (!ALLOWED_EXECUTORS.contains(executorType)) {
                throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem promover membros a representante.");
            }
        }

        /**
         * Valida se o usuário-alvo é elegível para promoção a representante.
         *
         * <p>Exige: usuário ativo, não removido, com {@code TypeUser} {@code STUDENT}
         * e vínculo atual com papel {@code STUDENT}.</p>
         *
         * @param targetUser usuário a ser promovido.
         * @param membership vínculo atual do usuário com a turma.
         * @throws ClassMembershipException se qualquer condição não for atendida.
         */
        public void validateTargetUserForPromotion(UserEntity targetUser, ClassMembershipEntity membership) {
            if (!targetUser.isActive() || targetUser.getDeletedAt() != null) {
                throw new ClassMembershipException("Usuário está inativo ou excluído.");
            }
            if (targetUser.getTypeUser() != TypeUser.STUDENT) {
                throw new ClassMembershipException("Apenas usuários com TypeUser STUDENT podem ser promovidos a REPRESENTATIVE.");
            }
            if (membership.getClassRole() != ClassRole.STUDENT) {
                throw new ClassMembershipException("Apenas matrículas com o papel STUDENT podem ser promovidas a REPRESENTATIVE.");
            }
        }

        /**
         * Valida disponibilidade de vaga de representante na turma.
         *
         * <p>O limite máximo é de dois representantes simultâneos por turma.</p>
         *
         * @param currentCount quantidade atual de representantes na turma.
         * @throws ClassMembershipException se o limite de dois representantes já for atingido.
         */
        public void validateRepresentativeSlotAvailable (long currentCount) {
            if (currentCount >= 2) {
                throw new ClassMembershipException("A turma já atingiu o número máximo de representantes ativos.");
            }
        }

        /**
         * Valida se o executor pode rebaixar um representante.
         *
         * @param executorType tipo do usuário que executa a operação.
         * @throws UserPermissionDeniedException se o executor não for {@code ADMIN} ou {@code SENAI}.
         */
        public void validateExecutorCanDemote(TypeUser executorType) {
            if (!ALLOWED_EXECUTORS.contains(executorType)) {
                throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem remover um representante.");
            }
        }

        /**
         * Valida se o vínculo é elegível para rebaixamento de representante.
         *
         * <p>Exige: vínculo ativo, papel atual {@code REPRESENTATIVE}
         * e {@code TypeUser} do usuário {@code REPRESENTATIVE}.</p>
         *
         * @param membership vínculo atual do usuário com a turma.
         * @throws ClassMembershipException se qualquer condição não for atendida.
         */
        public void validateTargetUserForDemotion(ClassMembershipEntity membership) {
            if (!membership.isActive()) {
                throw new ClassMembershipException("Usuário ou turma está inativo ou excluído.");
            }
            if (membership.getClassRole() != ClassRole.REPRESENTATIVE) {
                throw new ClassMembershipException("Apenas matrículas com o papel REPRESENTATIVE podem ser rebaixadas.");
            }
            if (membership.getUser().getTypeUser() != TypeUser.REPRESENTATIVE) {
                throw new ClassMembershipException("Apenas usuários com TypeUser REPRESENTATIVE podem ser rebaixados.");
            }
        }

        /**
         * Valida se o executor pode remover o vínculo de um membro.
         *
         * <p>Impede auto-remoção: o executor não pode remover o próprio vínculo.</p>
         *
         * @param executorType tipo do usuário que executa a operação.
         * @param executorId   ID do executor.
         * @param targetUserId ID do usuário cujo vínculo será removido.
         * @throws UserPermissionDeniedException se o executor não for {@code ADMIN} ou {@code SENAI}.
         * @throws ClassMembershipException      se executor e alvo forem o mesmo usuário.
         */
        public void validateExecutorCanDeleteMembership(TypeUser executorType, UUID executorId, UUID targetUserId) {
            if (!ALLOWED_EXECUTORS.contains(executorType)) {
                throw new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem remover matrículas da turma.");
            }
            if (executorId.equals(targetUserId)) {
                throw new ClassMembershipException("O usuário não pode remover a própria matrícula.");
            }
        }
    }
