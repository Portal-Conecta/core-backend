package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Caso de uso responsável por listar todas as salas físicas disponíveis no sistema.
 */
@Component
public class GetAllRoomUseCase {

    private final RoomRepository roomRepository;

    public GetAllRoomUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Executa a listagem geral das salas.
     * Retorna apenas os registros que estão ativos no catálogo, ocultando aqueles
     * que sofreram exclusão lógica (soft delete).
     *
     * @return Lista contendo todas as entidades RoomEntity ativas.
     */
    public List<RoomEntity> execute(){
        return roomRepository.findAllByDeletedAtIsNull();
    }
}
