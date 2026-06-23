package com.portal.conecta.hub.module.classes.application.use_case.membership;

import com.portal.conecta.hub.module.classes.application.command.DemoteMemberCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassMembershipValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class DemoteFromRepresentativeUseCase {

    private final RequestContextProvider requestProvider;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassMembershipRepository membershipRepository;
    private final ClassMembershipValidator membershipValidator;


    public DemoteFromRepresentativeUseCase(
            RequestContextProvider requestProvider,
            ClassRepository classRepository,
            UserRepository userRepository,
            ClassMembershipRepository membershipRepository,
            ClassMembershipValidator membershipValidator) {
        this.requestProvider = requestProvider;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.membershipValidator = membershipValidator;
    }

    @Transactional
    public ClassMembershipEntity execute(DemoteMemberCommand command) {
        RequestContext context = requestProvider.getRequestContext();

        membershipValidator.validateExecutorCanDemote(context.userType());

        ClassMembershipEntity membership = membershipRepository
                .findById(new ClassMembershipId(command.userId(), command.classId()))
                .orElseThrow(() -> new ClassMembershipNotFoundException("O usuário não possui uma matrícula ativa nesta turma."));

        membershipValidator.validateTargetUserForDemotion(membership);

        UserEntity executor = userRepository.findById(context.userId())
                .orElseThrow(()-> new UserNotFoundException("Executor não encontrado. "));

        membership.demoteToStudent(executor);
        membershipRepository.save(membership);
        log.info("");
        return membership;
    }
}
