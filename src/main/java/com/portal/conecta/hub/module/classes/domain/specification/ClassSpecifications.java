package com.portal.conecta.hub.module.classes.domain.specification;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

/**
 * Specifications para filtragem dinâmica de turmas via JPA Criteria API.
 *
 * <p>As specifications primitivas ({@link #isNotDeleted}, {@link #isActive},
 * {@link #isInactive}, {@link #idIn}) podem ser compostas livremente.
 * Os métodos de composição ({@link #withActiveFilter} e {@link #byIdsWithActiveFilter})
 * cobrem os cenários de listagem e consulta em lote do módulo.</p>
 */
public final class ClassSpecifications {

    private ClassSpecifications() {
    }

    /**
     * Exclui turmas removidas logicamente ({@code deletedAt IS NULL}).
     * Base obrigatória para qualquer consulta que não deva expor registros excluídos.
     */

    public static Specification<ClassEntity> isNotDeleted() {
        return (root, query, cb) ->
                cb.isNull(root.get("deletedAt"));
    }

    /**
     * Filtra apenas turmas com {@code active = true}.
     */
    public static Specification<ClassEntity> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }

    /**
     * Filtra apenas turmas com {@code active = false}.
     */
    public static Specification<ClassEntity> isInactive() {
        return (root, query, cb) ->
                cb.isFalse(root.get("active"));
    }

    /**
     * Filtra turmas cujo ID esteja na coleção informada.
     *
     * @param ids coleção de identificadores; duplicatas são tratadas pelo banco.
     */
    public static Specification<ClassEntity> idIn(
            Collection<UUID> ids
    ) {
        return (root, query, cb) ->
                root.get("id").in(ids);
    }

    /**
     * Compõe filtro de estado para listagens gerais de turmas.
     *
     * <p>Sempre exclui turmas removidas logicamente. O comportamento varia conforme os parâmetros:</p>
     * <ul>
     *   <li>{@code onlyInactive=true} — retorna apenas turmas inativas (ignora {@code includeInactive}).</li>
     *   <li>{@code includeInactive=false} — retorna apenas turmas ativas.</li>
     *   <li>{@code includeInactive=true} — retorna turmas ativas e inativas.</li>
     * </ul>
     *
     * @param includeInactive quando {@code true}, inclui turmas inativas no resultado.
     * @param onlyInactive    quando {@code true}, retorna exclusivamente turmas inativas.
     */
    public static Specification<ClassEntity> withActiveFilter(
            boolean includeInactive,
            boolean onlyInactive
    ) {
        Specification<ClassEntity> specification = isNotDeleted();

        if (onlyInactive) {
            return specification.and(isInactive());
        }

        if (!includeInactive) {
            return specification.and(isActive());
        }

        return specification;
    }

    /**
     * Compõe filtro de IDs com filtro de estado para consultas em lote.
     *
     * <p>Sempre aplica {@link #isNotDeleted}. Quando {@code includeInactive=false},
     * restringe adicionalmente a turmas ativas.</p>
     *
     * @param ids             coleção de identificadores das turmas.
     * @param includeInactive quando {@code true}, inclui turmas inativas no resultado.
     */
    public static Specification<ClassEntity> byIdsWithActiveFilter(
            Collection<UUID> ids,
            boolean includeInactive
    ) {
        return idIn(ids)
                .and(withActiveFilter(includeInactive, false));
    }
}