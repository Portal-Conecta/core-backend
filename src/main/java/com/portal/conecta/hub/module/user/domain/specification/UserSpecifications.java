package com.portal.conecta.hub.module.user.domain.specification;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.criteria.Subquery;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtragem dinamica de usuarios via JPA Criteria API.
 */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<UserEntity> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<UserEntity> hasType(TypeUser typeUser) {
        return (root, query, cb) -> cb.equal(root.get("type"), typeUser);
    }

    public static Specification<UserEntity> nameContains(String name) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("name")),
                "%" + name.trim().toLowerCase() + "%"
        );
    }

    public static Specification<UserEntity> isNotMemberOfClass(UUID classId) {
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            var membership = subquery.from(ClassMembershipEntity.class);

            subquery.select(membership.get("user").get("id"))
                    .where(cb.equal(membership.get("classEntity").get("id"), classId));

            return cb.not(root.get("id").in(subquery));
        };
    }

    public static Specification<UserEntity> fromFilters(
            TypeUser typeUser,
            String name,
            UUID excludeClassId
    ) {
        Specification<UserEntity> specification = isNotDeleted();

        if (typeUser != null) {
            specification = specification.and(hasType(typeUser));
        }

        if (name != null && !name.isBlank()) {
            specification = specification.and(nameContains(name));
        }

        if (excludeClassId != null) {
            specification = specification.and(isNotMemberOfClass(excludeClassId));
        }

        return specification;
    }
}
