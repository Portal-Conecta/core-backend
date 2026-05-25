package com.portal.conecta.hub.module.user.presentation.dto.request;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ListUsersRequest(
        @Min(value = 0, message = "page must be greater than or equal to 0.")
        Integer page,

        @Min(value = 1, message = "size must be greater than or equal to 1.")
        @Max(value = 100, message = "size must be less than or equal to 100.")
        Integer size,

        TypeUser typeUser
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public GetAllUserQuery toQuery() {
        return new GetAllUserQuery(resolvePage(), resolveSize(), typeUser);
    }

    private int resolvePage() {
        if (page == null) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    private int resolveSize() {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        return size;
    }
}
