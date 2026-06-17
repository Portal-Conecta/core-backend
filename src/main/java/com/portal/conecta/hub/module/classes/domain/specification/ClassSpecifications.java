package com.portal.conecta.hub.module.classes.domain.specification;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

public final class ClassSpecifications {

    private ClassSpecifications() {
    }

    public static Specification<ClassEntity> isNotDeleted() {
        return (root, query, cb) ->
                cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ClassEntity> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }

    public static Specification<ClassEntity> isInactive() {
        return (root, query, cb) ->
                cb.isFalse(root.get("active"));
    }

    public static Specification<ClassEntity> idIn(
            Collection<UUID> ids
    ) {
        return (root, query, cb) ->
                root.get("id").in(ids);
    }

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

    public static Specification<ClassEntity> byIdsWithActiveFilter(
            Collection<UUID> ids,
            boolean includeInactive
    ) {
        return idIn(ids)
                .and(withActiveFilter(includeInactive, false));
    }
}