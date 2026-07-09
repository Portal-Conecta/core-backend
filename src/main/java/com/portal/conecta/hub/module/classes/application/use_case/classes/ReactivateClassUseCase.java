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

import java.util.Objects;
import java.util.UUID;

/**
 * Reativa uma turma previamente desativada, retornando-a aos fluxos normais do sistema.
 *
 * <p>Opera sobre turmas desativadas por {@link DeactivateClassUseCase}.
 * Não deve ser confundida com {@link RestoreClassUseCase}, que opera sobre
 * turmas removidas logicamente.</p>
 */
@Component
@Slf4j
public class ReactivateClassUseCase {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;
    private final ClassEventPublisher classEventPublisher;

    public ReactivateClassUseCase(
            ClassRepository classRepository,
            UserRepository userRepository,
            ClassPermissionValidator permissionValidator,
            RequestContextProvider contextProvider, ClassEventPublisher classEventPublisher
    ) {
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
        this.classEventPublisher = classEventPublisher;
    }

    /**
     * Executa a reativação da turma.
     *
     * @param classId identificador da turma a ser reativada.
     * @return entidade da turma após reativação.
     * @throws ClassEntityNotFoundException  se a turma não for encontrada.
     * @throws UserNotFoundException         se o usuário autenticado não for encontrado na base.
     */
    @Transactional
    public ClassEntity execute(UUID classId) {
        Objects.requireNonNull(classId, "O identificador da turma é obrigatório.");

        RequestContext context = contextProvider.getRequestContext();
        permissionValidator.validateCanReactivate(context.userType());

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(ClassEntityNotFoundException::new);

        UserEntity executor = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        classEntity.reactivate(executor);

        ClassEntity saved = classRepository.save(classEntity);
        log.info("Turma reativada com sucesso.");
        classEventPublisher.publishCreated(saved);
        return saved;
    }
}
