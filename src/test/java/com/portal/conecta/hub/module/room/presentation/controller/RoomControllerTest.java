package com.portal.conecta.hub.module.room.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.hub.module.room.application.command.CreateRoomCommand;
import com.portal.conecta.hub.module.room.application.command.UpdateRoomCommand;
import com.portal.conecta.hub.module.room.application.use_case.CreateRoomUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetAllRoomUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetRoomByIdUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetRoomsBulkUseCase;
import com.portal.conecta.hub.module.room.application.use_case.UpdateRoomUseCase;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNumberAlreadyInUseException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.presentation.dto.BulkRoomRequest;
import com.portal.conecta.hub.module.room.presentation.dto.BulkRoomResponse;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomRequest;
import com.portal.conecta.hub.module.room.presentation.dto.RoomResponse;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private CreateRoomUseCase createRoomUseCase;

    @Mock
    private GetAllRoomUseCase getAllRoomUseCase;

    @Mock
    private GetRoomByIdUseCase getRoomByIdUseCase;

    @Mock
    private UpdateRoomUseCase updateRoomUseCase;

    @Mock
    private GetRoomsBulkUseCase getRoomsBulkUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RoomController(createRoomUseCase, getAllRoomUseCase, getRoomByIdUseCase, updateRoomUseCase, getRoomsBulkUseCase))
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
    @DisplayName("GET /rooms/{id} deve retornar 404 quando sala não existe ou está removida")
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
        CreateRoomRequest request = new CreateRoomRequest(101, TypeRoom.CLASSROOM);
        RoomEntity room = new RoomEntity(101, TypeRoom.CLASSROOM);

        when(createRoomUseCase.execute(any())).thenReturn(room);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(createRoomUseCase).execute(any());
    }

    @Test
    @DisplayName("POST /rooms deve retornar 403 quando usuário não tem permissão")
    void shouldReturn403WhenUserLacksPermissionOnCreate() throws Exception {
        when(createRoomUseCase.execute(any())).thenThrow(new RoomPermissionDeniedException());

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRoomRequest(101, TypeRoom.CLASSROOM))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /rooms deve retornar 409 quando número da sala já está em uso")
    void shouldReturn409WhenRoomNumberAlreadyInUseOnCreate() throws Exception {
        when(createRoomUseCase.execute(any())).thenThrow(new RoomNumberAlreadyInUseException(101));

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRoomRequest(101, TypeRoom.CLASSROOM))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /rooms/bulk deve retornar 200 com items, foundIds e missingIds")
    void shouldReturn200ForBulkRequest() throws Exception {
        UUID validId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();

        RoomEntity validRoom = new RoomEntity(101, TypeRoom.CLASSROOM);
        ReflectionTestUtils.setField(validRoom, "id", validId);

        BulkRoomResponse response = new BulkRoomResponse(
                List.of(RoomResponse.from(validRoom)),
                List.of(validId),
                List.of(missingId)
        );

        when(getRoomsBulkUseCase.execute(anyList())).thenReturn(response);

        mockMvc.perform(post("/rooms/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BulkRoomRequest(List.of(validId, missingId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(validId.toString()))
                .andExpect(jsonPath("$.foundIds[0]").value(validId.toString()))
                .andExpect(jsonPath("$.missingIds[0]").value(missingId.toString()));

        verify(getRoomsBulkUseCase).execute(anyList());
    }

    @Test
    @DisplayName("PATCH /rooms/{roomId} deve atualizar sala e retornar 200")
    void updateReturns200WithUpdatedData() throws Exception {
        UUID roomId = UUID.randomUUID();
        Instant updatedAt = Instant.parse("2026-05-28T10:00:00Z");

        RoomEntity room = new RoomEntity(204, TypeRoom.LABORATORY);
        ReflectionTestUtils.setField(room, "id", roomId);
        ReflectionTestUtils.setField(room, "updatedAt", updatedAt);

        when(updateRoomUseCase.execute(any(UpdateRoomCommand.class))).thenReturn(room);

        mockMvc.perform(patch("/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "number": 204, "type": "LABORATORY" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roomId.toString()))
                .andExpect(jsonPath("$.number").value(204))
                .andExpect(jsonPath("$.status").value("active"));

        verify(updateRoomUseCase).execute(any());
    }

    @Test
    @DisplayName("PATCH /rooms/{roomId} deve atualizar apenas o type")
    void updatePartialOnlyType() throws Exception {
        UUID roomId = UUID.randomUUID();
        RoomEntity room = new RoomEntity(101, TypeRoom.LABORATORY);
        ReflectionTestUtils.setField(room, "id", roomId);

        when(updateRoomUseCase.execute(any(UpdateRoomCommand.class))).thenReturn(room);

        mockMvc.perform(patch("/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "LABORATORY" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(101));

        verify(updateRoomUseCase).execute(any());
    }

    @Test
    @DisplayName("PATCH /rooms/{roomId} deve atualizar apenas o number")
    void updatePartialOnlyNumber() throws Exception {
        UUID roomId = UUID.randomUUID();
        RoomEntity room = new RoomEntity(505, TypeRoom.CLASSROOM);
        ReflectionTestUtils.setField(room, "id", roomId);

        when(updateRoomUseCase.execute(any(UpdateRoomCommand.class))).thenReturn(room);

        mockMvc.perform(patch("/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "number": 505 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(505));

        verify(updateRoomUseCase).execute(any());
    }

    @Test
    @DisplayName("PATCH /rooms/{roomId} deve retornar 403 quando usuário não tem permissão")
    void updateReturns403WhenUserLacksPermission() throws Exception {
        when(updateRoomUseCase.execute(any())).thenThrow(new RoomPermissionDeniedException());

        mockMvc.perform(patch("/rooms/{roomId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "number": 204 }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /rooms/{roomId} deve retornar 404 quando sala não existe ou está removida")
    void updateReturns404WhenRoomDoesNotExist() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(updateRoomUseCase.execute(any()))
                .thenThrow(new RoomNotFoundException("Room not found: " + roomId));

        mockMvc.perform(patch("/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "number": 204 }
                                """))
                .andExpect(status().isNotFound());

        verify(updateRoomUseCase).execute(any());
    }

    @Test
    @DisplayName("PATCH /rooms/{roomId} deve retornar 400 quando nenhum campo é informado")
    void updateReturns400WhenNoFieldProvided() throws Exception {
        mockMvc.perform(patch("/rooms/{roomId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /rooms/{roomId} deve retornar 400 quando type é inválido")
    void updateReturns400WhenTypeIsInvalid() throws Exception {
        mockMvc.perform(patch("/rooms/{roomId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "INVALIDO" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /rooms/{roomId} deve retornar 409 quando número já está em uso")
    void updateReturns409WhenNumberAlreadyExists() throws Exception {
        when(updateRoomUseCase.execute(any()))
                .thenThrow(new RoomNumberAlreadyInUseException(204));

        mockMvc.perform(patch("/rooms/{roomId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "number": 204 }
                                """))
                .andExpect(status().isConflict());
    }
}