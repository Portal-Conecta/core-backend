package com.portal.conecta.hub.module.room.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.hub.module.room.application.use_case.CreateRoomUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetAllRoomUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetRoomByIdUseCase;
import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomRequest;
import com.portal.conecta.hub.module.room.presentation.mapper.RoomMapper;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private CreateRoomUseCase createRoomUseCase;

    @Mock
    private GetAllRoomUseCase getAllRoomUseCase;

    @Mock
    private GetRoomByIdUseCase getRoomByIdUseCase;

    @Mock
    private RoomMapper roomMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RoomController(createRoomUseCase, getAllRoomUseCase, getRoomByIdUseCase, roomMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /rooms deve retornar lista de salas ativas")
    void shouldReturnActiveRooms() throws Exception {
        RoomEntity room1 = new RoomEntity(101, TypeRoom.CLASSROOM);
        RoomEntity room2 = new RoomEntity(202, TypeRoom.LABORATORY);

        when(getAllRoomUseCase.execute()).thenReturn(List.of(room1, room2));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(getAllRoomUseCase).execute();
    }

    @Test
    @DisplayName("GET /rooms deve retornar lista vazia quando não há salas ativas")
    void shouldReturnEmptyListWhenNoActiveRooms() throws Exception {
        when(getAllRoomUseCase.execute()).thenReturn(List.of());

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(getAllRoomUseCase).execute();
    }

    @Test
    @DisplayName("GET /rooms/{id} deve retornar sala ativa quando encontrada")
    void shouldReturnRoomWhenFound() throws Exception {
        UUID roomId = UUID.randomUUID();
        RoomEntity room = new RoomEntity(101, TypeRoom.CLASSROOM);

        when(getRoomByIdUseCase.execute(roomId)).thenReturn(room);

        mockMvc.perform(get("/rooms/{id}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(101))
                .andExpect(jsonPath("$.status").value("active"));

        verify(getRoomByIdUseCase).execute(roomId);
    }

    @Test
    @DisplayName("GET /rooms/{id} deve retornar 404 quando sala não existe")
    void shouldReturn404WhenRoomNotFound() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(getRoomByIdUseCase.execute(roomId))
                .thenThrow(new RoomNotFoundException("Room not found: " + roomId));

        mockMvc.perform(get("/rooms/{id}", roomId))
                .andExpect(status().isNotFound());

        verify(getRoomByIdUseCase).execute(roomId);
    }

    @Test
    @DisplayName("POST /rooms deve criar sala e retornar 201")
    void shouldCreateRoomAndReturn201() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest(101, "classroom");
        RoomEntity room = new RoomEntity(101, TypeRoom.CLASSROOM);

        when(roomMapper.toCommand(any())).thenReturn(
                new com.portal.conecta.hub.module.room.application.command.CreateRoomCommand(101, TypeRoom.CLASSROOM)
        );
        when(createRoomUseCase.execute(any())).thenReturn(room);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(createRoomUseCase).execute(any());
    }
}