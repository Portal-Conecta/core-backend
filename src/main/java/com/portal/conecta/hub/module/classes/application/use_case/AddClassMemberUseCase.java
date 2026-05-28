package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.AddMemberCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
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
            ClassMembershipValidator membershipValidator) {
        this.requestProvider = requestProvider;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.membershipValidator = membershipValidator;
    }


    @Transactional
    public ClassMembershipEntity execute(AddMemberCommand command) {
        RequestContext context = requestProvider.getRequestContext();

       membershipValidator.validateExecutorCanAddMember(
               context.userType(), context.userId(), command.userId(), command.classRole()
       );

       ClassEntity classEntity = classRepository.findById(command.classId())
               .orElseThrow(() -> new ClassNotFoundException("Class not found: " + command.classId()));
       membershipValidator.validateClassIsActive(classEntity);

       UserEntity targetUser = userRepository.findById(command.userId())
               .orElseThrow(()-> new UserNotFoundException("User not found: " + command.userId()));
       membershipValidator.validateTargetUserCanBeAdded(targetUser, command.classRole());

       boolean duplicateExists = membershipRepository
               .existsByUserIdAndClassId(command.userId(), command.classId());
       membershipValidator.validateNoDuplicateMembership(duplicateExists);

       if (command.classRole() == ClassRole.STUDENT) {
           long existingStudentClasses = membershipRepository
                   .countByUserIdAndClassRole(command.userId(),command.classRole());
           membershipValidator.validateStudentClassLimit(command.classRole(), existingStudentClasses);
       }

       ClassMembershipEntity membership = new ClassMembershipEntity(targetUser, classEntity, command.classRole());
       return membershipRepository.save(membership);

    }
}
