package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.application.command.RestoreRoomCommand;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.room.domain.validator.RoomPermissionValidator;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Caso de uso responsável por reverter a exclusão lógica de uma sala (restauração).
 */
@Component
@Slf4j
public class RestoreRoomUseCase {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RequestContextProvider requestContextProvider;
    private final RoomPermissionValidator roomPermissionValidator;

    public RestoreRoomUseCase(RoomRepository roomRepository, UserRepository userRepository, RequestContextProvider requestContextProvider, RoomPermissionValidator roomPermissionValidator) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.requestContextProvider = requestContextProvider;
        this.roomPermissionValidator = roomPermissionValidator;
    }

    /**
     * Processa a restauração de uma sala previamente deletada.
     * Verifica o privilégio do usuário e se a sala está, de fato, em estado de remoção.
     *
     * @param command Comando contendo o identificador da sala.
     * @return A entidade Room com o estado reativado.
     * @throws RoomPermissionDeniedException se o usuário não possuir privilégios administrativos.
     * @throws RoomNotFoundException se o registro da sala não existir fisicamente no banco.
     * @throws InvalidRoomDataException se a sala ainda estiver ativa.
     */
    @Transactional
    public RoomEntity execute(RestoreRoomCommand command) {
        RequestContext context = requestContextProvider.getRequestContext();

        validatePermission(context);

        UUID roomId = command.roomId();

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        if (room.isActive()) {
            throw new InvalidRoomDataException("A sala não está removida.");
        }

        UserEntity executor = userRepository.findById(context.userId())
                .orElseThrow(() -> new InvalidRoomDataException("Usuário não encontrado!"));

        room.restore(executor);

        RoomEntity saved = roomRepository.save(room);

        log.info("Sala restaurada com sucesso. roomId={}, requesterUserId={}",
                saved.getId(), context.userId());

        return saved;
    }

    private void validatePermission(RequestContext context) {
        if (!roomPermissionValidator.canRestore(context.userType())) {
            throw new RoomPermissionDeniedException();
        }
    }
}
