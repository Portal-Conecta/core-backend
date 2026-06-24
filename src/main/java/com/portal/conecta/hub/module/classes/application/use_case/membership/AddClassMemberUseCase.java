package com.portal.conecta.hub.module.classes.application.use_case.membership;

import com.portal.conecta.hub.module.classes.application.command.AddMemberCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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
               .orElseThrow(ClassEntityNotFoundException::new);

        if(classEntity.isDeleted()){
            throw new ClassMembershipException("A turma foi excluída e não pode receber novos membros.");
        }

       UserEntity targetUser = userRepository.findById(command.userId())
               .orElseThrow(UserNotFoundException::new);
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
       log.info("Membro associado à uma turma com sucesso. [targetUser={}], [classId={}] , [classRole={}]", targetUser, classEntity.getId(),  command.classRole());
       return membershipRepository.save(membership);

    }
}
