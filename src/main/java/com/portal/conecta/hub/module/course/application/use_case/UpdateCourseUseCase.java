package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.application.command.UpdateCourseCommand;
import com.portal.conecta.hub.module.course.domain.exception.CourseCodeAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.CourseNameAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseEventPublisher;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.course.domain.validator.CoursePermissionValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso responsável pela atualização parcial (PATCH) de um curso existente.
 * <p>
 * Durante a atualização, a verificação de unicidade para nome e código ignora o
 * ID do próprio curso sendo editado. Cursos que sofreram exclusão lógica não podem ser modificados.
 *
 * @throws UserPermissionDeniedException se o usuário atual não possuir permissão de edição.
 * @throws CourseNotFoundException se o ID do curso não existir na base.
 * @throws CourseNameAlreadyInUseException se o novo nome fornecido já pertencer a outro curso.
 * @throws CourseCodeAlreadyInUseException se o novo código fornecido já pertencer a outro curso.
 */
@Slf4j
@Component
public class UpdateCourseUseCase {

    private final UserRepository userRepository;
    private final RequestContextProvider requestProvider;
    private final CoursePermissionValidator permissionValidator;
    private final CourseRepository courseRepository;
    private final CourseEventPublisher courseEventPublisher;

    public UpdateCourseUseCase (UserRepository userRepository,
                                RequestContextProvider requestProvider,
                                CoursePermissionValidator permissionValidator,
                                CourseRepository courseRepository, CourseEventPublisher courseEventPublisher) {
        this.userRepository = userRepository;
        this.requestProvider = requestProvider;
        this.permissionValidator = permissionValidator;
        this.courseRepository = courseRepository;
        this.courseEventPublisher = courseEventPublisher;
    }

    @Transactional
    public CourseEntity execute(UpdateCourseCommand courseCommand) {
        RequestContext context = requestProvider.getRequestContext();

        if (!permissionValidator.canUpdate(context.userType())) {
            throw new UserPermissionDeniedException("Usuário não tem permissão para editar este curso.");
        }

        UserEntity updatedBy = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        CourseEntity course = courseRepository.findById(courseCommand.courseId())
                .orElseThrow(CourseNotFoundException::new);

        course.validateNotDeleted();

        if (courseCommand.name() != null && courseRepository.existsByNameAndIdNot(courseCommand.name(), course.getId())) {
            throw new CourseNameAlreadyInUseException(courseCommand.name());
        }

        if (courseCommand.code() != null && courseRepository.existsByCodeAndIdNot(courseCommand.code(), course.getId())) {
            throw new CourseCodeAlreadyInUseException(courseCommand.code());
        }

        List<String> changedFields = course.update(courseCommand.name(), courseCommand.code(), updatedBy);

        CourseEntity saved = courseRepository.save(course);
        courseEventPublisher.publishUpdated(saved);

        log.info("Curso atualizado com sucesso. courseId={}, changedFields={}",
                saved.getId(), changedFields);

        return saved;
    }
}