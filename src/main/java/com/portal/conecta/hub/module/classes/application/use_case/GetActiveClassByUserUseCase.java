package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ActiveClassNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
public class GetActiveClassByUserUseCase {

    private final UserRepository userRepository;
    private final ClassMembershipRepository classMembershipRepository;

    public GetActiveClassByUserUseCase(
            UserRepository userRepository,
            ClassMembershipRepository classMembershipRepository
    ) {
        this.userRepository = userRepository;
        this.classMembershipRepository = classMembershipRepository;
    }

    public List<ClassMembershipEntity> execute(GetActiveClassByUserCommand command) {
        var user = userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(command.userId())
                .orElseThrow(ActiveClassNotFoundException::new);

        List<ClassMembershipEntity> memberships = classMembershipRepository.findEligibleActiveByUserIdAndRoles(
                user.getId(),
                EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE)
        );

        if (memberships.isEmpty()) {
            throw new ActiveClassNotFoundException();
        }

        return memberships;
    }
}