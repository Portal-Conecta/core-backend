package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.room.presentation.dto.BulkRoomResponse;
import com.portal.conecta.hub.module.room.presentation.dto.RoomResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class GetRoomsBulkUseCase {

    private final RoomRepository roomRepository;


    public GetRoomsBulkUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public BulkRoomResponse execute (List<UUID> ids) {
        Objects.requireNonNull(ids, "ids is required");

        List<UUID> uniqueIds = ids.stream().distinct().toList();

        List<RoomEntity> found = roomRepository.findAllByIdInAndDeletedAtIsNull(uniqueIds);

        List<UUID> foundIds =
                found.stream()
                        .map(RoomEntity::getId)
                        .toList();

        List<UUID> missingIds =
                uniqueIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .toList();

        List<RoomResponse> items =
                found.stream()
                        .map(RoomResponse::from)
                        .toList();

        return new BulkRoomResponse(
                items,
                foundIds,
                missingIds
        );
    }
}
