package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.AddMemberCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassMembershipValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class AddClassMemberUseCase {

    private final RequestContextProvider requestProvider;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassMembershipRepository membershipRepository;
    private final ClassMembershipValidator membershipValidator;

    public AddClassMemberUseCase(
            RequestContextProvider requestProvider,
            ClassRepository classRepository,
            UserRepository userRepository,
            ClassMembershipRepository membershipRepository,
            ClassMembershipValidator merbershipValidator) {
        this.requestProvider = requestProvider;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.membershipValidator = merbershipValidator;
    }


    @Transactional
    public ClassMembershipEntity execute(AddMemberCommand command) {
        RequestContext context = requestProvider.getRequestContext();

        membershipValidator.validateExecutorType(context.userType());
        membershipValidator.validateClassRoleNotRepresentative(command.classRole());
        membershipValidator.validateNoSelfAssociation(context.userId(), command.userId());

        ClassEntity classEntity = classRepository.findById(command.classId())
                .orElseThrow(() -> new ClassNotFoundException("Class not found: " + command.classId()));

        if(classEntity.getDeletedAt() != null){
            throw new ClassMembershipException("Class is deleted and cannot receive new members.");
        }

        UserEntity targetUser = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + command.userId()));

        if(!targetUser.isActive() || targetUser.getDeletedAt() != null) {
            throw new ClassMembershipException("User is inactive or deleted.");
        }

        membershipValidator.validateTargetUserType(targetUser.getTypeUser());
        membershipValidator.validateTypeAndRoleCombination(targetUser.getTypeUser(), command.classRole());

        boolean duplicateExists = membershipRepository
                .existsByUserIdAndClassId(command.userId(), command.classId());
        membershipValidator.validateNoDuplicateMembership(duplicateExists);

        long existingStudentsClasses = membershipRepository
                .countByUserIdAndClassRole(command.userId(), command.classRole());
        membershipValidator.validateStudentClassLimit(command.classRole(),existingStudentsClasses);

        ClassMembershipEntity membership = new ClassMembershipEntity(targetUser, classEntity, command.classRole());
        return membershipRepository.save(membership);

    }

}
