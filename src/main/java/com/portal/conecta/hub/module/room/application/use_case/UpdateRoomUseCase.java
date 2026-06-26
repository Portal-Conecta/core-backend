package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.application.command.UpdateRoomCommand;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNumberAlreadyInUseException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.room.domain.validator.RoomPermissionValidator;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class UpdateRoomUseCase {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final GetRoomByIdUseCase getRoomByIdUseCase;
    private final RequestContextProvider contextProvider;
    private final RoomPermissionValidator permissionValidator;

    public UpdateRoomUseCase(
            RoomRepository roomRepository,
            UserRepository userRepository,
            GetRoomByIdUseCase getRoomByIdUseCase,
            RoomPermissionValidator permissionValidator,
            RequestContextProvider contextProvider
    ) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.getRoomByIdUseCase = getRoomByIdUseCase;
        this.contextProvider = contextProvider;
        this.permissionValidator = permissionValidator;
    }

    @Transactional
    public RoomEntity execute(UpdateRoomCommand command) {
        RequestContext context = contextProvider.getRequestContext();

        validateAtLeastOneField(command);
        validatePermission(context);

        RoomEntity room = getRoomByIdUseCase.execute(command.roomId());

        validateNumberUniqueness(command, room);

        UserEntity editor = userRepository.findById(context.userId())
                .orElseThrow(() -> new InvalidRoomDataException("Usuário autenticado não encontrado."));

        List<String> changedFields = room.update(command.number(), command.typeRoom(), editor);

        RoomEntity saved = roomRepository.save(room);

        log.info("Sala atualizada com sucesso. roomId={}, requesterUserId={}, changedFields={}",
                saved.getId(), context.userId(), changedFields);

        return saved;
    }

    private void validatePermission(RequestContext context) {
        if (!permissionValidator.canUpdate(context.userType())) {
            throw new RoomPermissionDeniedException();
        }
    }

    private void validateAtLeastOneField(UpdateRoomCommand command) {
        if (command.number() == null && command.typeRoom() == null) {
            throw new InvalidRoomDataException("Pelo menos um campo deve ser informado.");
        }
    }

    private void validateNumberUniqueness(UpdateRoomCommand command, RoomEntity room) {
        if (command.number() == null) return;
        if (command.number().equals(room.getNumber())) return;
        if (roomRepository.existsByNumberAndIdNot(command.number(), room.getId())) {
            throw new RoomNumberAlreadyInUseException(command.number());
        }
    }
}