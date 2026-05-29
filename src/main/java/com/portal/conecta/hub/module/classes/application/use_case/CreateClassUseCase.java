package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.CreateClassCommand;
import com.portal.conecta.hub.module.course.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassPermissionValidator;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateClassUseCase {

    private final ClassRepository classRepository;
    private final ClassPermissionValidator permissionValidator;
    private final RequestContextProvider requestProvider;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CreateClassUseCase(
            ClassRepository classRepository, ClassPermissionValidator permissionValidator,
            RequestContextProvider requestProvider, CourseRepository courseRepository,
            UserRepository userRepository
    ) {
        this.classRepository = classRepository;
        this.permissionValidator = permissionValidator;
        this.requestProvider = requestProvider;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ClassEntity execute(CreateClassCommand command) {
        CreateClassCommand validCommand = requireCommand(command);
        RequestContext context = requestProvider.getRequestContext();

        if (!permissionValidator.canCreate(context.userType())) {
            throw new UnauthorizedUserException();
        }

        CourseEntity course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException("Course not found: " + command.courseId()));

        UserEntity createdBy = userRepository.findById(context.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + context.userId()));

        int nextNumber = classRepository.findLastNumberByCourseId(command.courseId())
                .map(last -> last + 1)
                .orElse(1);

        ClassEntity classEntity = ClassEntity.create(
                command.shift(),
                nextNumber,
                course,
                createdBy
        );

        return classRepository.save(classEntity);
    }

    private CreateClassCommand requireCommand(CreateClassCommand command){
        if (command == null){
            throw new InvalidClassDataException("");
        }
        return command;
    }
}
