package com.portal.conecta.hub.module.classes.application.use_case.membership;

import com.portal.conecta.hub.module.classes.application.command.DeleteMembershipCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassMembershipValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Remove o vínculo de um usuário com uma turma.
 *
 * <p>Se o membro removido for representante, seu papel de usuário é rebaixado
 * para {@code STUDENT} antes da exclusão do vínculo. Essa operação não afeta
 * o estado da turma nem de outros membros.</p>
 */
@Component
@Slf4j
public class DeleteClassMembershipUseCase {

    private final RequestContextProvider requestProvider;
    private final UserRepository userRepository;
    private final ClassMembershipRepository membershipRepository;
    private final ClassMembershipValidator membershipValidator;

    public DeleteClassMembershipUseCase(
            RequestContextProvider requestProvider,
            UserRepository userRepository,
            ClassMembershipRepository membershipRepository,
            ClassMembershipValidator membershipValidator) {
        this.requestProvider = requestProvider;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.membershipValidator = membershipValidator;
    }

    /**
     * Executa a remoção do vínculo.
     *
     * @param command identificadores da turma e do usuário cujo vínculo será removido.
     * @throws ClassMembershipNotFoundException se o vínculo não for encontrado.
     * @throws UserNotFoundException          se o executor não for encontrado ao rebaixar um representante.
     */
    @Transactional
    public void execute(DeleteMembershipCommand command) {
        RequestContext context = requestProvider.getRequestContext();

        membershipValidator.validateExecutorCanDeleteMembership(context.userType(), context.userId(), command.userId());

        ClassMembershipEntity membership = membershipRepository
                .findById(new ClassMembershipId(command.userId(), command.classId()))
                .orElseThrow(()-> new ClassMembershipNotFoundException("Matrícula não encontrada."));

        if(membership.getClassRole() == ClassRole.REPRESENTATIVE){
            UserEntity executor = userRepository
                    .findById(context.userId())
                    .orElseThrow(() -> new UserNotFoundException("Executor não encontrado. "));

            membership.getUser().demoteTo(TypeUser.STUDENT,executor);
        }
        membershipRepository.delete(membership);
        log.info("Vínculo de turma removido com sucesso. [classId={}], [targetUser={}]", command.classId(), command.userId());
    }

}
