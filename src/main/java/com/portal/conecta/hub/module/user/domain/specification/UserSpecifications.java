package com.portal.conecta.hub.module.user.domain.specification;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.CriteriaBuilder;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Specifications composaveis para a busca administrativa de usuarios. */
public final class UserSpecifications {

    private static final List<TypeUser> ACADEMIC_USER_TYPES = List.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE);
    private static final List<ClassRole> ACADEMIC_CLASS_ROLES = List.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE);

    private UserSpecifications() {
    }

    public static Specification<UserEntity> from(GetAllUserQuery query) {
        Specification<UserEntity> specification = Specification.where(accountStatusIn(query.accountStatuses()))
                .and(hasType(query.typeUser()))
                .and(nameContains(query.name()));

        return query.semTurmaAtiva() ? specification.and(hasNoActiveAcademicClass()) : specification;
    }

    private static Specification<UserEntity> accountStatusIn(List<com.portal.conecta.hub.module.user.domain.model.AccountStatus> statuses) {
        return (root, criteriaQuery, criteriaBuilder) -> root.get("accountStatus").in(statuses);
    }

    private static Specification<UserEntity> hasType(TypeUser type) {
        return type == null ? Specification.unrestricted() : (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
    }

    private static Specification<UserEntity> nameContains(String name) {
        return name == null ? Specification.unrestricted() : (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<UserEntity> hasNoActiveAcademicClass() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                root.get("type").in(ACADEMIC_USER_TYPES).not(),
                criteriaBuilder.not(criteriaBuilder.exists(activeAcademicMembershipFor(root, criteriaQuery, criteriaBuilder)))
        );
    }

    private static Subquery<Integer> activeAcademicMembershipFor(
            Root<UserEntity> user,
            jakarta.persistence.criteria.CriteriaQuery<?> criteriaQuery,
            CriteriaBuilder criteriaBuilder
    ) {
        Subquery<Integer> subquery = criteriaQuery.subquery(Integer.class);
        Root<ClassMembershipEntity> membership = subquery.from(ClassMembershipEntity.class);
        var classEntity = membership.join("classEntity");

        subquery.select(criteriaBuilder.literal(1));
        subquery.where(
                criteriaBuilder.equal(membership.get("user"), user),
                membership.get("classRole").in(ACADEMIC_CLASS_ROLES),
                criteriaBuilder.isTrue(classEntity.get("active")),
                criteriaBuilder.isNull(classEntity.get("deletedAt"))
        );
        return subquery;
    }
}
