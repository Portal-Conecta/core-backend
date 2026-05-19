package com.portal.conecta.hub.module.user.presentation.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.application.use_case.CreateUserUseCase;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CreateUserUseCase createUserUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(createUserUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturnsCreatedUserWithoutSensitiveData() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-19T18:00:00Z");
        UserEntity createdUser = new UserEntity(
                "Student One",
                "student@estudante.sesisenai.org.br",
                "encoded-secret",
                TypeUser.STUDENT
        );
        ReflectionTestUtils.setField(createdUser, "id", userId);
        ReflectionTestUtils.setField(createdUser, "createdAt", createdAt);
        ReflectionTestUtils.setField(createdUser, "updatedAt", createdAt);

        when(createUserUseCase.execute(any(CreateUserCommand.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Student One",
                                  "email": "student@estudante.sesisenai.org.br",
                                  "password": "secret",
                                  "typeUser": "STUDENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + userId))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Student One"))
                .andExpect(jsonPath("$.email").value("student@estudante.sesisenai.org.br"))
                .andExpect(jsonPath("$.typeUser").value("STUDENT"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").value("2026-05-19T18:00:00Z"))
                .andExpect(jsonPath("$.deletedAt").doesNotExist())
                .andExpect(jsonPath("$", not(hasKey("password"))))
                .andExpect(jsonPath("$", not(hasKey("passwordHash"))));

        ArgumentCaptor<CreateUserCommand> commandCaptor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(createUserUseCase).execute(commandCaptor.capture());

        CreateUserCommand command = commandCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> org.junit.jupiter.api.Assertions.assertEquals("Student One", command.name()),
                () -> org.junit.jupiter.api.Assertions.assertEquals("student@estudante.sesisenai.org.br", command.email()),
                () -> org.junit.jupiter.api.Assertions.assertEquals("secret", command.password()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(TypeUser.STUDENT, command.typeUser())
        );
    }

    @Test
    void createReturnsConflictWhenEmailIsAlreadyInUse() throws Exception {
        when(createUserUseCase.execute(any(CreateUserCommand.class))).thenThrow(new EmailAlreadyInUseException());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Student One",
                                  "email": "student@estudante.sesisenai.org.br",
                                  "password": "secret",
                                  "typeUser": "STUDENT"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already in use."));
    }
}
