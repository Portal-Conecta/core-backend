package com.portal.conecta.hub.module.room.domain.port;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência para a entidade Room.
 */
public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {

    /**
     * Verifica a existência global de um número de sala, incluindo registros excluídos.
     */
    boolean existsByNumber(Integer number);

    /**
     * Verifica se um número de sala já pertence a outro ID no banco (útil para atualizações parciais).
     */
    boolean existsByNumberAndIdNot(Integer number, UUID id);

    /**
     * Recupera todas as salas que não sofreram exclusão lógica (status ativo).
     */
    List<RoomEntity> findAllByDeletedAtIsNull();

    /**
     * Busca uma sala ativa específica pelo identificador. Ignora registros marcados como deletados.
     */
    Optional<RoomEntity> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Busca um lote de salas ativas filtrando por uma lista de identificadores.
     */
    List<RoomEntity> findAllByIdInAndDeletedAtIsNull(List<UUID> ids);

}