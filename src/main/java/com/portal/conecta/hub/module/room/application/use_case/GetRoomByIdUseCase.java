package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso responsável por consultar detalhadamente uma sala ativa pelo seu identificador.
 */
@Component
public class GetRoomByIdUseCase {

    private final RoomRepository roomRepository;

    public GetRoomByIdUseCase(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Executa a busca. Exige que a sala exista e não esteja em estado de exclusão lógica.
     *
     * @param id O identificador único da sala.
     * @return A entidade Room ativa correspondente.
     * @throws RoomNotFoundException se o identificador não existir ou pertencer a uma sala deletada.
     */
    public RoomEntity execute(UUID id){
        Objects.requireNonNull(id, "O identificador da sala é obrigatório.");
        return roomRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(RoomNotFoundException::new);
    }
}
