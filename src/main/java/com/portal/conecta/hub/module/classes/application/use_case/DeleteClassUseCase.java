package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassEventPublisher;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassPermissionValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeleteClassUseCase {

    private final ClassRepository classRepository;
    private final ClassPermissionValidator permissionValidator;
    private final RequestContextProvider requestProvider;
    private final UserRepository userRepository;
    private final ClassEventPublisher classEventPublisher;


    public DeleteClassUseCase(ClassRepository classRepository, ClassPermissionValidator permissionValidator, RequestContextProvider requestProvider, UserRepository userRepository, ClassEventPublisher classEventPublisher) {
        this.classRepository = classRepository;
        this.permissionValidator = permissionValidator;
        this.requestProvider = requestProvider;
        this.userRepository = userRepository;
        this.classEventPublisher = classEventPublisher;
    }

    @Transactional
    public void execute(UUID classId){
        RequestContext context = requestProvider.getRequestContext();

        permissionValidator.validateCanDelete(context.userType());

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(ClassEntityNotFoundException::new);

        UserEntity deletedBy = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        classEntity.delete(deletedBy);

        classRepository.save(classEntity);
        classEventPublisher.publishDeleted(classEntity);
    }
}
