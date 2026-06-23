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

@Component
public class GetClassesBulkUseCase {

    private final ClassRepository classRepository;

    public GetClassesBulkUseCase(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

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
