package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class GetRoomByIdUseCase {

    private final RoomRepository roomRepository;

    public GetRoomByIdUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public RoomEntity execute(UUID id){
        Objects.requireNonNull(id, "room is required");
        return roomRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(()-> new RoomNotFoundException("Room not found: " + id));
    }
}
