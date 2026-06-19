package com.portal.conecta.hub.module.classes.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.application.use_case.GetActiveClassByUserUseCase;
import com.portal.conecta.hub.module.classes.domain.exception.ActiveClassNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserActiveClassControllerTest {

    @Mock
    private GetActiveClassByUserUseCase getActiveClassByUserUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserActiveClassController(getActiveClassByUserUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /users/{userId}/class deve retornar 200 com o UUID da turma ativa")
    void shouldReturnActiveClassId() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        when(getActiveClassByUserUseCase.execute(any(GetActiveClassByUserCommand.class)))
                .thenReturn(classId);

        mockMvc.perform(get("/users/{userId}/class", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + classId + "\""));
    }

    @Test
    @DisplayName("GET /users/{userId}/class deve retornar 404 quando não houver turma ativa elegível")
    void shouldReturn404WhenNoActiveClass() throws Exception {
        UUID userId = UUID.randomUUID();

        when(getActiveClassByUserUseCase.execute(any(GetActiveClassByUserCommand.class)))
                .thenThrow(new ActiveClassNotFoundException("Nenhuma turma ativa encontrada para o usuário informado."));

        mockMvc.perform(get("/users/{userId}/class", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /users/{userId}/class deve retornar 404 quando usuário não existir, estiver inativo ou removido")
    void shouldReturn404WhenUserUnavailable() throws Exception {
        UUID userId = UUID.randomUUID();

        when(getActiveClassByUserUseCase.execute(any(GetActiveClassByUserCommand.class)))
                .thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/users/{userId}/class", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}