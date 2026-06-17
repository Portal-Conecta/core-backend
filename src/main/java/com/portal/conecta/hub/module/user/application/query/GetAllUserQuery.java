package com.portal.conecta.hub.module.user.application.query;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public record GetAllUserQuery(
        int page,
        int size,
        TypeUser typeUser
) {

    private static final int MAX_SIZE = 100;
    private static final Sort DEFAULT_SORT = Sort.by("createdAt").descending().and(Sort.by("id").ascending());

    public GetAllUserQuery {
        if (page < 0) {
            throw new InvalidUserDataException("page deve ser maior ou igual a 0.");
        }

        if (size < 1) {
            throw new InvalidUserDataException("size deve ser maior ou igual a 1.");
        }

        if (size > MAX_SIZE) {
            throw new InvalidUserDataException("size deve ser menor ou igual a 100.");
        }
    }

    public PageRequest toPageRequest() {
        return PageRequest.of(page, size, DEFAULT_SORT);
    }
}
