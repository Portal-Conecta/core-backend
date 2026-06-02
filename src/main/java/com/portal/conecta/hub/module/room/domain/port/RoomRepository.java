package com.portal.conecta.hub.module.room.domain.port;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
    boolean existsByNumber(Integer number);

    List<RoomEntity> findAllByDeletedAtIsNull();

    Optional<RoomEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<RoomEntity> findAllByIdInAndDeletedAtIsNull(List<UUID> ids);

}