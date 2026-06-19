package com.portal.conecta.hub.module.classes.application.use_case;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ActiveClassNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class GetActiveClassByUserUseCase {

    private static final EnumSet<ClassRole> ELIGIBLE_ROLES = EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE);

    private final UserRepository userRepository;
    private final ClassMembershipRepository classMembershipRepository;

    public GetActiveClassByUserUseCase(
            UserRepository userRepository,
            ClassMembershipRepository classMembershipRepository
    ) {
        this.userRepository = userRepository;
        this.classMembershipRepository = classMembershipRepository;
    }

    public UUID execute(GetActiveClassByUserCommand command) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(command.userId())
                .orElseThrow(UserNotFoundException::new);

        List<ClassMembershipEntity> eligibleMemberships = classMembershipRepository
                .findEligibleActiveByUserIdAndRoles(user.getId(), ELIGIBLE_ROLES);

        return eligibleMemberships.stream()
                .findFirst()
                .map(membership -> membership.getClassEntity().getId())
                .orElseThrow(ActiveClassNotFoundException::new);
    }
}