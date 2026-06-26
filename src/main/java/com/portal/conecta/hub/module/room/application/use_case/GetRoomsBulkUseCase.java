package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.room.presentation.dto.BulkRoomResponse;
import com.portal.conecta.hub.module.room.presentation.dto.RoomResponse;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Caso de uso dedicado à busca de salas em lote.
 */
@Component
public class GetRoomsBulkUseCase {

    private final RoomRepository roomRepository;


    public GetRoomsBulkUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Resolve uma lista de identificadores, retornando as salas ativas encontradas e isolando
     * os identificadores que não puderam ser localizados ou que estão removidos.
     * Identificadores duplicados na entrada são sanitizados.
     *
     * @param ids Lista bruta de UUIDs solicitados.
     * @return Estrutura contendo as salas mapeadas, os IDs processados com sucesso e os IDs ausentes.
     */
    public BulkRoomResponse execute (List<UUID> ids) {
        Objects.requireNonNull(ids, "ids is required");

        List<UUID> uniqueIds = ids.stream().distinct().toList();

        List<RoomEntity> found = roomRepository.findAllByIdInAndDeletedAtIsNull(uniqueIds);

        List<UUID> foundIds =
                found.stream()
                        .map(RoomEntity::getId)
                        .toList();

        Set<UUID> foundIdsSet = new HashSet<>(foundIds);

        List<UUID> missingIds =
                uniqueIds.stream()
                        .filter(id -> !foundIdsSet.contains(id))
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
