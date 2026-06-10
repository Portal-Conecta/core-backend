package com.portal.conecta.hub.module.classes.presentation.dto.request;

import com.portal.conecta.hub.module.classes.application.query.ListClassesQuery;

public record ListClassesRequest(

        Integer page,
        Integer size,
        Boolean includeInactive,
        Boolean onlyInactive
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public ListClassesQuery toQuery (){
        return new ListClassesQuery(
                page!= null ? page: DEFAULT_PAGE,
                size!= null ? size: DEFAULT_SIZE,
                Boolean.TRUE.equals(includeInactive),
                Boolean.TRUE.equals(onlyInactive)
        );
    }
}
