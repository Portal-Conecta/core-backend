package com.portal.conecta.hub.module.room.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.portal.conecta.hub.module.room.application.command.CreateRoomCommand;
import com.portal.conecta.hub.module.room.application.use_case.CreateRoomUseCase;
import com.portal.conecta.hub.module.room.domain.exception.RoomNumberAlreadyInUseException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.presentation.mapper.RoomMapper;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private CreateRoomUseCase createRoomUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RoomController(createRoomUseCase, new RoomMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturns201WithRoomData() throws Exception {
        UUID roomId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-28T10:00:00Z");

        UserEntity admin = new UserEntity("Admin", "admin@portal.test", "hash", TypeUser.ADMIN);
        RoomEntity room = new RoomEntity(101, TypeRoom.CLASSROOM);
        room.setCreatedBy(admin);
        ReflectionTestUtils.setField(room, "id", roomId);
        ReflectionTestUtils.setField(room, "createdAt", createdAt);

        when(createRoomUseCase.execute(any(CreateRoomCommand.class))).thenReturn(room);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "number": 101,
                                  "type": "classroom"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/rooms/" + roomId))
                .andExpect(jsonPath("$.id").value(roomId.toString()))
                .andExpect(jsonPath("$.number").value(101))
                .andExpect(jsonPath("$.type").value("classroom"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.createdAt").value("2026-05-28T10:00:00Z"));
    }

    @Test
    void createReturns403WhenUserLacksPermission() throws Exception {
        when(createRoomUseCase.execute(any())).thenThrow(new RoomPermissionDeniedException());

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "number": 101,
                                  "type": "classroom"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have permission to create a room."));
    }

    @Test
    void createReturns400WhenNumberIsNull() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "classroom"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("number is required."));
    }

    @Test
    void createReturns400WhenTypeIsInvalid() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "number": 101,
                                  "type": "invalid"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("type 'invalid' is not valid."));
    }

    @Test
    void createReturns409WhenNumberAlreadyExists() throws Exception {
        when(createRoomUseCase.execute(any()))
                .thenThrow(new RoomNumberAlreadyInUseException(101));

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "number": 101,
                                  "type": "classroom"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Room number '101' is already in use."));
    }
}