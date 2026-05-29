package com.portal.conecta.hub.module.room.presentation.controller;

import com.portal.conecta.hub.module.room.application.use_case.CreateRoomUseCase;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomRequest;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomResponse;
import com.portal.conecta.hub.module.room.presentation.mapper.RoomMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final CreateRoomUseCase createRoomUseCase;
    private final RoomMapper roomMapper;

    public RoomController(CreateRoomUseCase createRoomUseCase, RoomMapper roomMapper) {
        this.createRoomUseCase = createRoomUseCase;
        this.roomMapper = roomMapper;
    }

    @PostMapping
    public ResponseEntity<CreateRoomResponse> create(@Valid @RequestBody CreateRoomRequest request) {
        var room = createRoomUseCase.execute(roomMapper.toCommand(request));
        return ResponseEntity.created(URI.create("/rooms/" + room.getId()))
                .body(CreateRoomResponse.from(room));
    }
}
