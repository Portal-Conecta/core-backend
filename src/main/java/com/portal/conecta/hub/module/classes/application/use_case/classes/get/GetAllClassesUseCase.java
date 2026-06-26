package com.portal.conecta.hub.module.classes.application.use_case.classes.get;

import com.portal.conecta.hub.module.classes.application.query.ListClassesQuery;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.specification.ClassSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retorna uma página de turmas aplicando filtros de estado via {@link ClassSpecifications}.
 *
 * <p>Por padrão, apenas turmas ativas são retornadas. O comportamento pode ser
 * ajustado via {@code includeInactive} e {@code onlyInactive} na query.</p>
 */
@Component
public class GetAllClassesUseCase {

    private final ClassRepository classRepository;

    public GetAllClassesUseCase(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    /**
     * Executa a listagem paginada de turmas.
     *
     * @param query parâmetros de paginação, ordenação e filtros de estado.
     * @return página de turmas conforme os critérios informados.
     */
    @Transactional(readOnly = true)
    public Page<ClassEntity> execute (ListClassesQuery query){

        Specification<ClassEntity> spec = ClassSpecifications.withActiveFilter(
                query.includeInactive(),
                query.onlyInactive()
        );

        return classRepository.findAll(spec, query.toPageRequest());
    }
}
