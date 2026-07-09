package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.application.command.RemoveRoomCommand;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
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

/**
 * Caso de uso responsável por aplicar a exclusão lógica (soft delete) a uma sala ativa.
 */
@Component
@Slf4j
public class RemoveRoomUseCase {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RequestContextProvider requestContextProvider;
    private final RoomPermissionValidator roomPermissionValidator;

    public RemoveRoomUseCase(
            RoomRepository roomRepository,
            UserRepository userRepository,
            RequestContextProvider requestContextProvider,
            RoomPermissionValidator roomPermissionValidator
    ) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.requestContextProvider = requestContextProvider;
        this.roomPermissionValidator = roomPermissionValidator;
    }

    /**
     * Remove a sala do catálogo de salas ativas.
     * O registro físico é mantido por questões de auditoria e consistência histórica.
     *
     * @param command Comando contendo o identificador da sala a ser removida.
     * @throws RoomPermissionDeniedException se o usuário não possuir privilégios para deleção.
     * @throws RoomNotFoundException se a sala não existir no banco.
     * @throws InvalidRoomDataException se a sala já se encontrar no estado deletado.
     */
    @Transactional
    public void execute(RemoveRoomCommand command) {
        RequestContext context = requestContextProvider.getRequestContext();

        validatePermission(context);

        RoomEntity room = roomRepository.findById(command.roomId())
                .orElseThrow(RoomNotFoundException::new);

        if (!room.isActive()) {
            throw new InvalidRoomDataException("A sala já foi removida.");
        }

        UserEntity executor = userRepository.findById(context.userId())
                .orElseThrow(() ->
                        new InvalidRoomDataException("Usuário não encontrado!"));

        room.delete(executor);

        roomRepository.save(room);

        log.info("Sala removida logicamente com sucesso. roomId={}, requesterUserId={}",
                room.getId(), context.userId());
    }

    private void validatePermission(RequestContext context) {
        if (!roomPermissionValidator.canRemove(context.userType())) {
            throw new RoomPermissionDeniedException();
        }
    }
}