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
import org.springframework.stereotype.Component;

@Component
public class RemoveRoomUseCase {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final RequestContextProvider requestContextProvider;
    private final RoomPermissionValidator roomPermissionValidator;

    public RemoveRoomUseCase(RoomRepository roomRepository, UserRepository userRepository, RequestContextProvider requestContextProvider, RoomPermissionValidator roomPermissionValidator) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.requestContextProvider = requestContextProvider;
        this.roomPermissionValidator = roomPermissionValidator;
    }

    @Transactional
    public void execute(RemoveRoomCommand command){
        RequestContext context = requestContextProvider.getRequestContext();
        validatePermission(context);
        RoomEntity room = roomRepository.findById(command.roomId())
                .orElseThrow(() -> new RoomNotFoundException("Room not found: " + command.roomId()));
        if (!room.isActive()){
            throw new InvalidRoomDataException("The room has already been removed.");
        }
        UserEntity executor = userRepository.getReferenceById(context.userId());
        room.delete(executor);
        roomRepository.save(room);
    }
    private void validatePermission(RequestContext context) {
        if (!roomPermissionValidator.canRemove(context.userType())){
            throw new RoomPermissionDeniedException();
        }
    }
}
