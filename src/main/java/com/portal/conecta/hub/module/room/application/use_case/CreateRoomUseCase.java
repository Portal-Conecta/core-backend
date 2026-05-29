package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.application.command.CreateRoomCommand;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

@Component
public class CreateRoomUseCase {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;


    public CreateRoomUseCase(RoomRepository roomRepository, UserRepository userRepository, RoomPermissionValidator permissionValidator, RequestContextProvider contextProvider) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    @Transactional
    public RoomEntity execute(CreateRoomCommand command) {
        RequestContext context = contextProvider.getRequestContext();

        if (!permissionValidator.canCreate(context.userType())) throw new RoomPermissionDeniedException();

        if (roomRepository.existsByNumber(command.number())) {
            throw new RoomNumberAlreadyInUseException(command.number());
        }

        UserEntity creator = userRepository.findById(context.userId())
                .orElseThrow(() -> new InvalidRoomDataException("Authenticated user not found."));

        RoomEntity room = RoomEntity.create(command.number(), command.typeRoom(), creator);
        return roomRepository.save(room);
    }
}
