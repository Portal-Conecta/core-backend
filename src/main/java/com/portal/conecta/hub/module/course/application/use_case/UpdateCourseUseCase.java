package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.application.command.UpdateCourseCommand;
import com.portal.conecta.hub.module.course.domain.exception.*;
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
public class UpdateCourseUseCase {

    private final UserRepository userRepository;
    private final RequestContextProvider requestProvider;
    private final CoursePermissionValidator permissionValidator;
    private final CourseRepository courseRepository;

    public UpdateCourseUseCase (UserRepository userRepository,
                                RequestContextProvider requestProvider,
                                CoursePermissionValidator permissionValidator,
                                CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.requestProvider = requestProvider;
        this.permissionValidator = permissionValidator;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CourseEntity execute(UpdateCourseCommand courseCommand) {
        RequestContext context = requestProvider.getRequestContext();

        if (!permissionValidator.canUpdate(context.userType())) {
            throw new UnauthorizedUserException();
        }

        UserEntity updatedBy = userRepository.findById(context.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + context.userId()));

        CourseEntity course = courseRepository.findById(courseCommand.courseId())
                .orElseThrow(() -> new CourseEntityNotFoundException("Course not found: " + courseCommand.courseId()));

        course.validateNotDeleted();

        if (courseCommand.name() != null && courseRepository.existsByNameAndIdNot(courseCommand.name(), course.getId())) {
            throw new CourseNameAlreadyInUseException("Name already in use: " + courseCommand.name());
        }

        if (courseCommand.code() != null && courseRepository.existsByCodeAndIdNot(courseCommand.code(), course.getId())) {
            throw new CourseCodeAlreadyInUseException("Code already in use: " + courseCommand.code());
        }

        course.update(courseCommand.name(), courseCommand.code(), updatedBy);

        return courseRepository.save(course);
    }

}