package com.portal.conecta.hub.module.classes.presentation.dto.request;

import com.portal.conecta.hub.module.classes.application.query.ListClassesQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ListClassesRequest(

        @Min(value = 0, message = "page must be greater than or equal to 0.")
        Integer page,
        @Min(value = 1, message = "size must be greater than or equal to 1.")
        @Max(value = 100, message = "size must be less than or equal to 100.")
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
