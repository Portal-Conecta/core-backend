package com.portal.conecta.hub.module.classes.application.use_case.classes;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Realiza a exclusão lógica de uma turma.
 *
 * <p>A turma não é removida do banco — o campo {@code deletedAt} é preenchido,
 * tornando-a invisível nos fluxos normais. Diferente da desativação, a exclusão
 * lógica representa uma remoção intencional e definitiva dentro do ciclo de vida
 * da turma. A restauração pode ser feita via {@link RestoreClassUseCase}.</p>
 */
@Component
@Slf4j
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

    /**
     * Executa a exclusão lógica da turma.
     *
     * @param classId identificador da turma a ser removida logicamente.
     * @throws ClassEntityNotFoundException  se a turma não for encontrada.
     * @throws UserNotFoundException         se o usuário autenticado não for encontrado na base.
     */

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
        log.info("Turma removida logicamente com sucesso.");
        classEventPublisher.publishDeleted(classEntity);
    }
}
