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
 * Caso de Uso responsável pela criação e registro de novos cursos no sistema.
 * <p>
 * Esta classe orquestra o fluxo de negócio necessário para validar as permissões
 * do usuário logado, garantir que não haja duplicidade de código ou nome,
 * e disparar eventos após a persistência da entidade no banco de dados.
 * </p>
 *
 * @version 1.0
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

    /**
     * Executa a lógica de negócio para a criação de um novo curso.
     * <p>
     * O método valida o contexto da requisição (permissões e usuário), checa a
     * existência prévia de conflitos (nome e código) e gera um log e evento após a criação.
     * </p>
     *
     * @param courseCommand Objeto de comando contendo os dados do curso preenchidos na requisição (nome, código, etc.).
     * @return CourseEntity A entidade do curso recém-criada e persistida.
     * @throws UserPermissionDeniedException Se o usuário do contexto atual não tiver autorização para criar cursos.
     * @throws UserNotFoundException Se o usuário emissor da requisição não for encontrado na base.
     * @throws CourseNameAlreadyInUseException Se o nome fornecido no comando já estiver em uso.
     * @throws CourseCodeAlreadyInUseException Se o código fornecido no comando já estiver em uso.
     */
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