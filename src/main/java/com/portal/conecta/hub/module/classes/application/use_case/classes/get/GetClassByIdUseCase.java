package com.portal.conecta.hub.module.classes.application.use_case.classes.get;

import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Busca uma turma pelo identificador, excluindo turmas removidas logicamente.
 */
@Component
public class GetClassByIdUseCase {

    private final ClassRepository classRepository;

    public GetClassByIdUseCase(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    /**
     * Executa a busca da turma.
     *
     * @param classId identificador da turma.
     * @return entidade da turma encontrada.
     * @throws ClassEntityNotFoundException se a turma não existir ou estiver removida logicamente.
     */
    public ClassEntity execute(UUID classId){
        Objects.requireNonNull(classId, "O identificador da turma é obrigatório.");
        return classRepository.findByIdAndDeletedAtIsNull(classId)
                .orElseThrow(ClassEntityNotFoundException::new);
    }
}
