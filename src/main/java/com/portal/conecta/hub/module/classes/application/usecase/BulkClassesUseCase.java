package com.portal.conecta.hub.module.classes.application.usecase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.presentation.dto.BulkClassItemResponse;
import com.portal.conecta.hub.module.classes.presentation.dto.BulkClassesResponse;

@Service
public class BulkClassesUseCase {

    private final ClassRepository classRepository;

    public BulkClassesUseCase(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public BulkClassesResponse execute(List<UUID> ids, boolean includeInactive) {
        LinkedHashSet<UUID> uniqueIds = new LinkedHashSet<>(ids);

        Map<UUID, ClassEntity> classesById = classRepository.findAllByIdIn(uniqueIds)
                .stream()
                .filter(classEntity -> includeInactive || classEntity.isActive())
                .collect(Collectors.toMap(ClassEntity::getId, Function.identity(), (existing, ignored) -> existing));

        List<UUID> foundIds = new ArrayList<>(classesById.keySet());
        List<UUID> missingIds = uniqueIds.stream()
                .filter(id -> !classesById.containsKey(id))
                .toList();

        List<BulkClassItemResponse> items = classesById.values().stream()
                .map(classEntity -> new BulkClassItemResponse(classEntity.getId(), classEntity.isActive()))
                .toList();

        return new BulkClassesResponse(items, foundIds, missingIds);
    }
}
