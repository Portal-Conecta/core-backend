package com.portal.conecta.hub.module.user.presentation.dto.response;

import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import java.util.List;
import org.springframework.data.domain.Page;

public record ListUsersResponse(
        List<ListUserResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ListUsersResponse from(Page<UserEntity> users) {
        return new ListUsersResponse(
                users.getContent().stream()
                        .map(ListUserResponse::from)
                        .toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }
}
