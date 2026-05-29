package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllRoomUseCase {

    private final RoomRepository roomRepository;

    public GetAllRoomUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<RoomEntity> execute(){
        return roomRepository.findAllByDeletedAtIsNull();
    }
}
