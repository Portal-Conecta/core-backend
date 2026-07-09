package com.portal.conecta.hub.module.classes.application.use_case.classes.get;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.specification.ClassSpecifications;
import com.portal.conecta.hub.module.classes.presentation.dto.response.BulkClassResponse;
import com.portal.conecta.hub.module.classes.presentation.dto.response.ClassResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Consulta múltiplas turmas por lista de IDs em uma única operação.
 *
 * <p>IDs duplicados na entrada são ignorados. IDs não encontrados ou de turmas
 * desativadas (quando {@code includeInactive=false}) são retornados em {@code missingIds}
 * na resposta, permitindo que o chamador identifique lacunas sem lançar exceção.</p>
 */
@Component
public class GetClassesBulkUseCase {

    private final ClassRepository classRepository;

    public GetClassesBulkUseCase(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    /**
     * Executa a consulta em lote.
     *
     * @param ids             lista de identificadores das turmas; duplicatas são removidas internamente.
     * @param includeInactive quando {@code true}, inclui turmas desativadas no resultado.
     * @return resposta contendo turmas encontradas, IDs encontrados e IDs ausentes.
     */
    public BulkClassResponse execute (List<UUID> ids, boolean includeInactive){
        Objects.requireNonNull(ids, "Os identificadores das turmas são obrigatórios.");

        List<UUID> uniqueIds = ids.stream().distinct().toList();

        var specification =
                ClassSpecifications.byIdsWithActiveFilter(
                        uniqueIds,
                        includeInactive
                );

        List<ClassEntity> result =
                classRepository.findAll(specification);

        List<UUID> foundIds = result.stream()
                .map(ClassEntity::getId)
                .toList();

        List<UUID> missingIds =
                uniqueIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        List<ClassResponse> items = result.stream()
                .map(ClassResponse::from)
                .toList();

        return new BulkClassResponse(
                items,
                foundIds,
                missingIds
        );
    }
}
