package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse;
import com.portal.conecta.hub.module.user.presentation.dto.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetUsersBulkUseCase {

    private final UserRepository userRepository;

    public GetUsersBulkUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public BulkUserResponse execute(List<UUID> ids) {
        Objects.requireNonNull(ids, "ids is required");

        List<UUID> uniqueIds = ids.stream().distinct().toList();

        List<UserEntity> found = userRepository.findAllByIdInAndDeletedAtIsNullAndActiveTrue(uniqueIds);

        List<UUID> foundIds = found.stream()
                .map(UserEntity::getId)
                .toList();

        Set<UUID> foundIdsSet = new HashSet<>(foundIds);

        List<UUID> missingIds = uniqueIds.stream()
                .filter(id -> !foundIdsSet.contains(id))
                .toList();

        List<UserResponse> items = found.stream()
                .map(UserResponse::from)
                .toList();

        return new BulkUserResponse(items, foundIds, missingIds);

    }
}
