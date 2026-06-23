package com.portal.conecta.hub.module.classes.application.use_case.membership;

import com.portal.conecta.hub.module.classes.application.command.PromoteMemberCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
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
public class PromoteToRepresentativeUseCase {

    private final RequestContextProvider requestProvider;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassMembershipRepository membershipRepository;
    private final ClassMembershipValidator membershipValidator;

    public PromoteToRepresentativeUseCase(
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
    public ClassMembershipEntity execute(PromoteMemberCommand command) {
        RequestContext context = requestProvider.getRequestContext();

        membershipValidator.validateExecutorCanPromote(context.userType());

        ClassEntity classEntity = classRepository.findByIdForUpdate(command.classId())
                .orElseThrow(ClassEntityNotFoundException::new);

        if(classEntity.isDeleted()){
            throw new ClassMembershipException("A turma foi excluída e não pode receber novos membros.");
        }

        UserEntity targetUser = userRepository.findById(command.userId())
                .orElseThrow(UserNotFoundException::new);

        ClassMembershipEntity membership = membershipRepository
                .findById(new ClassMembershipId(command.userId(), command.classId()))
                .orElseThrow(() -> new ClassMembershipException("O usuário não possui uma matrícula ativa nesta turma."));

        membershipValidator.validateTargetUserForPromotion(targetUser, membership);

        long representativeCount = membershipRepository
                .countByClassIdAndClassRole(command.classId(), ClassRole.REPRESENTATIVE);
        membershipValidator.validateRepresentativeSlotAvailable(representativeCount);

        UserEntity executor = userRepository.findById(context.userId())
                .orElseThrow(()-> new UserNotFoundException("Executor não encontrado. "));

        membership.promoteToRepresentative(executor);
        membershipRepository.save(membership);
        log.info("Membro promovido a representante de turma. [classId={}], [targetUserId={}]", command.classId(), command.userId());
        return membership;
    }
}
