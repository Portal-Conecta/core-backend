package com.portal.conecta.hub.module.user.presentation.dto.request;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ListUsersRequest(
        @Min(value = 0, message = "A página deve ser maior ou igual a 0.")
        Integer page,

        @Min(value = 1, message = "O tamanho deve ser maior ou igual a 1.")
        @Max(value = 100, message = "O tamanho deve ser menor ou igual a 100.")
        Integer size,

        TypeUser typeUser,

        @Schema(description = "Filtra usuarios ativos cujo nome contenha o valor informado, sem diferenciar maiusculas de minusculas.", example = "Ana")
        String name
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public GetAllUserQuery toQuery() {
        return new GetAllUserQuery(resolvePage(), resolveSize(), typeUser, name);
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
