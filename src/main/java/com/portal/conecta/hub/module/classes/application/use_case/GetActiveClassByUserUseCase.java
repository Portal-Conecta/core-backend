package com.portal.conecta.hub.module.classes.application.use_case;

import java.util.List;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
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
        if (!userRepository.existsByIdAndDeletedAtIsNullAndActiveTrue(command.userId())) {
            throw new UserNotFoundException();
        }

        return classMembershipRepository.findActiveByUserId(command.userId());
    }
}