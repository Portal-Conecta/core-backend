package com.portal.conecta.hub.module.classes.domain.specification;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import org.springframework.data.jpa.domain.Specification;

public class ClassSpecifications {

    public static Specification<ClassEntity> isActive(){
        return (root, query, cb)
                -> cb.isTrue(root.get("active"));
    }

    public static Specification<ClassEntity> isInactive(){
        return ((root, query, cb)
                -> cb.isFalse(root.get("active")));
    }

    public static Specification<ClassEntity> withActiveFilter(boolean includeInactive, boolean onlyInactive){
        if (onlyInactive){
            return isInactive();
        }
        if (includeInactive){
            return ((root, query, cb)
                    -> cb.conjunction());
        }
        return isActive();
    }
}
