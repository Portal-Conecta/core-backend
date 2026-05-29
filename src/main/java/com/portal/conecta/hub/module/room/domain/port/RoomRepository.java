package com.portal.conecta.hub.module.room.domain.port;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
    boolean existsByNumber(Integer number);
}