package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
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
import java.util.UUID;

/**
 * Caso de uso responsável pela atualização parcial (PATCH) de um curso existente.
 * <p>
 * Durante a atualização, a verificação de unicidade para nome e código ignora o
 * ID do próprio curso sendo editado. Cursos que sofreram exclusão lógica não podem ser modificados.
 * </p>
 */
@Slf4j
@Component
public class UpdateCourseUseCase {

    private final UserRepository userRepository;
    private final RequestContextProvider requestProvider;
    private final CoursePermissionValidator permissionValidator;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final CourseEventPublisher courseEventPublisher;

    public UpdateCourseUseCase (UserRepository userRepository,
                                RequestContextProvider requestProvider,
                                CoursePermissionValidator permissionValidator,
                                CourseRepository courseRepository,
                                ClassRepository classRepository,
                                CourseEventPublisher courseEventPublisher) {
        this.userRepository = userRepository;
        this.requestProvider = requestProvider;
        this.permissionValidator = permissionValidator;
        this.courseRepository = courseRepository;
        this.classRepository = classRepository;
        this.courseEventPublisher = courseEventPublisher;
    }

    /**
     * Executa as regras de validação e processa a atualização dos dados do curso.
     * <p>
     * O método garante o nível de permissão adequado e confere se a entidade alvo não
     * está deletada. Por fim, rastreia os campos alterados, mescla os valores e emite um evento de atualização.
     * </p>
     *
     * @param courseCommand Objeto contendo o identificador do curso a ser alterado e os novos dados (nome e código).
     * @return CourseEntity A entidade do curso já refletindo os novos dados persistidos.
     * @throws UserPermissionDeniedException Se o usuário do contexto atual não possuir permissão de edição.
     * @throws UserNotFoundException Se o usuário logado não constar na base de dados.
     * @throws CourseNotFoundException Se o ID do curso a ser atualizado não existir na base.
     * @throws CourseNameAlreadyInUseException Se o novo nome fornecido já pertencer a outro curso diferente.
     * @throws CourseCodeAlreadyInUseException Se o novo código fornecido já pertencer a outro curso diferente.
     * @throws IllegalStateException (Ou outra RuntimeException de domínio) Se for tentado atualizar um curso logicamente excluído.
     */
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

        validateUniqueName(courseCommand, course);
        validateUniqueCode(courseCommand, course);

        List<String> changedFields = course.update(courseCommand.name(), courseCommand.code(), updatedBy);

        CourseEntity saved = courseRepository.save(course);
        int updatedClasses = updateClassNamesWhenCourseCodeChanged(saved, changedFields, context.userId());
        courseEventPublisher.publishUpdated(saved);

        log.info("Curso atualizado com sucesso. courseId={}, changedFields={}, updatedClasses={}",
                saved.getId(), changedFields, updatedClasses);

        return saved;
    }

    private void validateUniqueName(UpdateCourseCommand courseCommand, CourseEntity course) {
        if (courseCommand.name() != null && courseRepository.existsByNameAndIdNot(courseCommand.name(), course.getId())) {
            throw new CourseNameAlreadyInUseException(courseCommand.name());
        }
    }

    private void validateUniqueCode(UpdateCourseCommand courseCommand, CourseEntity course) {
        if (courseCommand.code() != null && courseRepository.existsByCodeAndIdNot(courseCommand.code(), course.getId())) {
            throw new CourseCodeAlreadyInUseException(courseCommand.code());
        }
    }

    private int updateClassNamesWhenCourseCodeChanged(CourseEntity course, List<String> changedFields, UUID updatedBy) {
        if (!changedFields.contains("code")) {
            return 0;
        }
        return classRepository.updateNamesByCourseId(course.getId(), course.getCode(), updatedBy);
    }
}
