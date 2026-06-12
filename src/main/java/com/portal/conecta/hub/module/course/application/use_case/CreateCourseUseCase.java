package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.application.command.CreateCourseCommand;
import com.portal.conecta.hub.module.course.domain.exception.CourseCodeAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.CourseNameAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.InvalidCourseDataException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.course.domain.validator.CoursePermissionValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateCourseUseCase {

    private final CourseRepository courseRepository;
    private final CoursePermissionValidator permissionValidator;
    private final RequestContextProvider requestProvider;
    private final UserRepository userRepository;

    public CreateCourseUseCase(CourseRepository courseRepository,
                               CoursePermissionValidator permissionValidator,
                               RequestContextProvider requestProvider,
                               UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.permissionValidator = permissionValidator;
        this.requestProvider = requestProvider;
        this.userRepository = userRepository;
    }

    @Transactional
    public CourseEntity execute(CreateCourseCommand courseCommand) {

        RequestContext context = requestProvider.getRequestContext();

        if (!permissionValidator.canCreate(context.userType())) {
            throw new UnauthorizedUserException();
        }

        UserEntity createdBy = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        String name = courseCommand.name().trim();
        String code = courseCommand.code().trim();

        if (courseRepository.existsByName(courseCommand.name())) {
            throw new CourseNameAlreadyInUseException(courseCommand.name());
        }

        if (courseRepository.existsByCode(courseCommand.code())) {
            throw new CourseCodeAlreadyInUseException(courseCommand.code());
        }

        CourseEntity course = CourseEntity.create(courseCommand.name(), courseCommand.code());
        course.setCreatedBy(createdBy);

        return courseRepository.save(course);
    }

}