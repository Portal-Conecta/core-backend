package com.portal.conecta.hub.module.classes.application.query;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record ListClassesQuery(
        int page,
        int size,
        boolean includeInactive,
        boolean onlyInactive
) {

    public Pageable toPageRequest(){
        return PageRequest.of(page, size);
    }
}
