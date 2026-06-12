package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassPermissionValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Component
public class RestoreClassUseCase {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;

    public RestoreClassUseCase(ClassRepository classRepository, UserRepository userRepository, ClassPermissionValidator permissionValidator, RequestContextProvider contextProvider) {
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    @Transactional
    public ClassEntity execute (UUID classId){
        Objects.requireNonNull(classId, "O identificador da turma é obrigatório.");

        RequestContext context = contextProvider.getRequestContext();
        permissionValidator.validateCanRestore(context.userType());

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(ClassEntityNotFoundException::new);

        UserEntity executor = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        classEntity.restore(executor);

        return classRepository.save(classEntity);
    }
}
