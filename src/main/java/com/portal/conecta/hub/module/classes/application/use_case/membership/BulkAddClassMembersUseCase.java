package com.portal.conecta.hub.module.classes.application.use_case.membership;

import com.portal.conecta.hub.module.classes.application.command.BulkAddMembersCommand;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Slf4j
public class BulkAddClassMembersUseCase {

    private final RequestContextProvider requestContextProvider;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassMembershipRepository classMembershipRepository;
    private final ClassMembershipValidator membershipValidator;


    public BulkAddClassMembersUseCase(RequestContextProvider requestContextProvider, ClassRepository classRepository,
                                      UserRepository userRepository, ClassMembershipRepository classMembershipRepository,
                                      ClassMembershipValidator membershipValidator) {
        this.requestContextProvider = requestContextProvider;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.classMembershipRepository = classMembershipRepository;
        this.membershipValidator = membershipValidator;
    }

    @Transactional
    public List<ClassMembershipEntity> execute (BulkAddMembersCommand command){
        RequestContext context = requestContextProvider.getRequestContext();

        ClassEntity classEntity = classRepository.findById(command.classId())
                .orElseThrow(ClassEntityNotFoundException::new);

        if (classEntity.isDeleted()){
            throw new ClassMembershipException("A turma foi excluída e não pode receber novos membros");
        }

        validateNoDuplicateUserIdsInRequest(command.members());

        List<ClassMembershipEntity> createdMembership = new ArrayList<>();

        for(BulkAddMembersCommand.Item item : command.members()){
            ClassMembershipEntity membership = addSingleMember(context, classEntity, item);
            createdMembership.add(membership);
        }
        log.info("Membros associados à turma em lote. [classId={}], [requestedCount={}], [createdCount={}]",
                command.classId(), command.members().size(), createdMembership.size());
        return createdMembership;
    }

    private ClassMembershipEntity addSingleMember (RequestContext context, ClassEntity classEntity, BulkAddMembersCommand.Item item){
        membershipValidator.validateExecutorCanAddMember(
                context.userType(), context.userId(), item.userId(), item.classRole()
        );

        UserEntity targetUser = userRepository.findById(item.userId())
                .orElseThrow(UserNotFoundException::new);
        membershipValidator.validateTargetUserCanBeAdded(targetUser, item.classRole());

        boolean duplicateExists = classMembershipRepository
                .existsByUserIdAndClassId(item.userId(), classEntity.getId());
        membershipValidator.validateNoDuplicateMembership(duplicateExists);

        if (item.classRole() == ClassRole.STUDENT){
            long existingStudentClasses = classMembershipRepository
                    .countByUserIdAndClassRole(item.userId(),item.classRole());
            membershipValidator.validateStudentClassLimit(item.classRole(), existingStudentClasses);
        }

        ClassMembershipEntity membership = new ClassMembershipEntity(targetUser, classEntity, item.classRole());
        return classMembershipRepository.save(membership);
    }

    private void validateNoDuplicateUserIdsInRequest(List<BulkAddMembersCommand.Item> members) {
        Set<UUID> seen = new HashSet<>();
        for (BulkAddMembersCommand.Item item : members) {
            if (!seen.add(item.userId())) {
                throw new ClassMembershipException("A requisição contém o mesmo usuário mais de uma vez.");
            }
        }
    }
}
