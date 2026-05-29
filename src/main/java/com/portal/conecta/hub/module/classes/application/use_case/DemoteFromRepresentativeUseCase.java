package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.DemoteMemberCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipNotFound;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
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
                .orElseThrow(() -> new ClassMembershipNotFound("User does not have an active membership in this class."));

        membershipValidator.validateTargetUserForDemotion(membership);

        UserEntity executor = userRepository.findById(context.userId())
                .orElseThrow(()-> new UserNotFoundException("Executor not found: " + context.userId()));

        membership.demoteToStudent(executor);
        return membershipRepository.save(membership);
    }
}
