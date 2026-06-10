package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.query.ListClassesQuery;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.specification.ClassSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
public class GetAllClassesUseCase {

    private final ClassRepository classRepository;

    public GetAllClassesUseCase(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClassEntity> execute (ListClassesQuery query){
        Objects.requireNonNull(query, "query is required");

        boolean onlyInactive = query.onlyInactive();
        boolean includeInactive = query.includeInactive();

        Specification<ClassEntity> spec = ClassSpecifications.withActiveFilter(
                query.includeInactive(),
                query.onlyInactive()
        );

        return classRepository.findAll(spec, query.toPageRequest());
    }
}
