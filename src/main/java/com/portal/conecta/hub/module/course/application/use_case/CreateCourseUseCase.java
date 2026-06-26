package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.application.command.CreateCourseCommand;
import com.portal.conecta.hub.module.course.domain.exception.CourseCodeAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.CourseNameAlreadyInUseException;
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

/**
 * Caso de uso responsável por orquestrar a criação de um novo curso.
 * <p>
 * Fluxo de execução:
 * 1. Verifica se o tipo do usuário logado tem permissão para criar cursos.
 * 2. Valida as garantias de unicidade: o nome e o código informados não podem existir na base.
 * 3. Persiste o curso e vincula o usuário criador para auditoria.
 * 4. Publica o evento de integração para notificar outros módulos/serviços.
 *
 * @throws UserPermissionDeniedException se o usuário atual não possuir permissão para esta ação.
 * @throws CourseNameAlreadyInUseException se já existir um curso registrado com o mesmo nome.
 * @throws CourseCodeAlreadyInUseException se já existir um curso registrado com o mesmo código.
 */
@Slf4j
@Component
public class CreateCourseUseCase {

    private final CourseRepository courseRepository;
    private final CoursePermissionValidator permissionValidator;
    private final RequestContextProvider requestProvider;
    private final UserRepository userRepository;
    private final CourseEventPublisher courseEventPublisher;

    public CreateCourseUseCase(CourseRepository courseRepository,
                               CoursePermissionValidator permissionValidator,
                               RequestContextProvider requestProvider,
                               UserRepository userRepository, CourseEventPublisher courseEventPublisher) {
        this.courseRepository = courseRepository;
        this.permissionValidator = permissionValidator;
        this.requestProvider = requestProvider;
        this.userRepository = userRepository;
        this.courseEventPublisher = courseEventPublisher;
    }

    @Transactional
    public CourseEntity execute(CreateCourseCommand courseCommand) {

        RequestContext context = requestProvider.getRequestContext();

        if (!permissionValidator.canCreate(context.userType())) {
            throw new UserPermissionDeniedException("O usuário não tem permissão para realizar essa operação.");
        }

        UserEntity createdBy = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        if (courseRepository.existsByName(courseCommand.name())) {
            throw new CourseNameAlreadyInUseException(courseCommand.name());
        }

        if (courseRepository.existsByCode(courseCommand.code())) {
            throw new CourseCodeAlreadyInUseException(courseCommand.code());
        }

        CourseEntity course = CourseEntity.create(courseCommand.name(), courseCommand.code());
        course.setCreatedBy(createdBy);

        CourseEntity saved = courseRepository.save(course);
        courseEventPublisher.publishCreated(saved);

        log.info("Curso criado com sucesso. courseId={}, courseCode={}",
                saved.getId(), saved.getCode());

        return saved;
    }

}