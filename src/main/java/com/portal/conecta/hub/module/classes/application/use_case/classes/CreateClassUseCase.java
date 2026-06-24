package com.portal.conecta.hub.module.classes.application.use_case.classes;

import com.portal.conecta.hub.module.classes.application.command.CreateClassCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassNumberAlreadyInUseException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassEventPublisher;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassPermissionValidator;
import com.portal.conecta.hub.module.course.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class CreateClassUseCase {

    private final ClassRepository classRepository;
    private final ClassPermissionValidator permissionValidator;
    private final RequestContextProvider requestProvider;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ClassEventPublisher classEventPublisher;

    public CreateClassUseCase(
            ClassRepository classRepository, ClassPermissionValidator permissionValidator,
            RequestContextProvider requestProvider, CourseRepository courseRepository,
            UserRepository userRepository, ClassEventPublisher classEventPublisher
    ) {
        this.classRepository = classRepository;
        this.permissionValidator = permissionValidator;
        this.requestProvider = requestProvider;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.classEventPublisher = classEventPublisher;
    }

    @Transactional
    public ClassEntity execute(CreateClassCommand command) {

        RequestContext context = requestProvider.getRequestContext();

        if (!permissionValidator.canCreate(context.userType())) {
            throw new UserPermissionDeniedException("Usuário não tem permissão para realizar essa operação.");
        }

        CourseEntity course = courseRepository.findById(command.courseId())
                .orElseThrow(CourseNotFoundException::new);

        boolean numberAlreadyExists = classRepository
                .existsByNumberAndCourseIdAndDeletedAtIsNull(command.number(),command.courseId());

        if(numberAlreadyExists){
            throw new ClassNumberAlreadyInUseException(command.number());
        }

        UserEntity createdBy = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);


        ClassEntity classEntity = ClassEntity.create(
                command.shift(),
                command.number(),
                course,
                createdBy
        );

        ClassEntity saved = classRepository.save(classEntity);
        log.info("Turma criada com sucesso.");
        classEventPublisher.publishCreated(saved);
        return saved;
    }
}
