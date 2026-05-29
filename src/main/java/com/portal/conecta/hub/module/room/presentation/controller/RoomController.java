package com.portal.conecta.hub.module.room.presentation.controller;

import com.portal.conecta.hub.module.room.application.use_case.CreateRoomUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetAllRoomUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetRoomByIdUseCase;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomRequest;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomResponse;
import com.portal.conecta.hub.module.room.presentation.dto.RoomResponse;
import com.portal.conecta.hub.module.room.presentation.mapper.RoomMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final CreateRoomUseCase createRoomUseCase;
    private final GetAllRoomUseCase getAllRoomUseCase;
    private final GetRoomByIdUseCase getRoomByIdUseCase;
    private final RoomMapper roomMapper;

    public RoomController(CreateRoomUseCase createRoomUseCase, GetAllRoomUseCase getAllRoomUseCase, GetRoomByIdUseCase getRoomByIdUseCase, RoomMapper roomMapper) {
        this.createRoomUseCase = createRoomUseCase;
        this.getAllRoomUseCase = getAllRoomUseCase;
        this.getRoomByIdUseCase = getRoomByIdUseCase;
        this.roomMapper = roomMapper;
    }

    @PostMapping
    public ResponseEntity<CreateRoomResponse> create(@RequestBody @Valid CreateRoomRequest request) {
        var room = createRoomUseCase.execute(roomMapper.toCommand(request));
        return ResponseEntity.created(URI.create("/rooms/" + room.getId()))
                .body(CreateRoomResponse.from(room));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> findAll(){
        List<RoomResponse> rooms = getAllRoomUseCase.execute()
                .stream()
                .map(RoomResponse::from)
                .toList();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> findById(@PathVariable UUID id){
        return ResponseEntity.ok(
                RoomResponse.from(getRoomByIdUseCase.execute(id))
        );
    }


}
