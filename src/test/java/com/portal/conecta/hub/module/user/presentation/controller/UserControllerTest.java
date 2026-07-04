package com.portal.conecta.hub.module.user.presentation.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.portal.conecta.hub.module.classes.application.use_case.classes.get.GetActiveClassByUserUseCase;
import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.application.command.DeactivateUserCommand;
import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.application.use_case.*;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CreateUserUseCase createUserUseCase;

    @Mock
    private GetAllUserUseCase getAllUserUseCase;

    @Mock
    private DeactivateUserUseCase deactivateUserUseCase;

    private MockMvc mockMvc;

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private GetUsersBulkUseCase getUsersBulkUseCase;

    @Mock
    private GetUserByIdUseCase getUserByIdUseCase;

    @Mock
    private GetActiveClassByUserUseCase getActiveClassByUserUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(
                        createUserUseCase,
                        getAllUserUseCase,
                        updateUserUseCase,
                        deactivateUserUseCase,
                        getUserByIdUseCase,
                        getUsersBulkUseCase,
                        getActiveClassByUserUseCase

                ))
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
        createdUser.delete(null);
        ReflectionTestUtils.setField(createdUser, "deletedAt", null);
        ReflectionTestUtils.setField(createdUser, "deletedBy", null);

        ReflectionTestUtils.setField(createdUser, "id", userId);
        ReflectionTestUtils.setField(createdUser, "createdAt", createdAt);
        ReflectionTestUtils.setField(createdUser, "updatedAt", createdAt);

        when(createUserUseCase.execute(any(CreateUserCommand.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Student One\",\"email\": \"student@estudante.sesisenai.org.br\",\"typeUser\": \"STUDENT\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + userId))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Student One"))
                .andExpect(jsonPath("$.email").value("student@estudante.sesisenai.org.br"))
                .andExpect(jsonPath("$.typeUser").value("STUDENT"))
                .andExpect(jsonPath("$.active").value(false))
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
                () -> org.junit.jupiter.api.Assertions.assertEquals(TypeUser.STUDENT, command.typeUser())
        );
    }

    @Test
    void createReturnsConflictWhenEmailIsAlreadyInUse() throws Exception {
        when(createUserUseCase.execute(any(CreateUserCommand.class)))
                .thenThrow(new EmailAlreadyInUseException("student@estudante.sesisenai.org.br"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Student One\",\"email\": \"student@estudante.sesisenai.org.br\",\"typeUser\": \"STUDENT\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    void listReturnsPagedUsersWithoutSensitiveData() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-19T18:00:00Z");

        UserEntity user = new UserEntity(
                "Student One",
                "student@estudante.sesisenai.org.br",
                "encoded-secret",
                TypeUser.STUDENT
        );

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "createdAt", createdAt);
        ReflectionTestUtils.setField(user, "updatedAt", createdAt);

        when(getAllUserUseCase.execute(any(GetAllUserQuery.class))).thenReturn(new PageImpl<>(
                List.of(user),
                PageRequest.of(1, 10),
                11
        ));

        mockMvc.perform(get("/users")
                        .param("page", "1")
                        .param("size", "10")
                        .param("typeUser", "STUDENT")
                        .param("name", "Student")
                        .param("excludeClassId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(userId.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Student One"))
                .andExpect(jsonPath("$.content[0].email").value("student@estudante.sesisenai.org.br"))
                .andExpect(jsonPath("$.content[0].typeUser").value("STUDENT"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-05-19T18:00:00Z"))
                .andExpect(jsonPath("$.content[0]", not(hasKey("password"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("passwordHash"))))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(2));

        ArgumentCaptor<GetAllUserQuery> queryCaptor = ArgumentCaptor.forClass(GetAllUserQuery.class);
        verify(getAllUserUseCase).execute(queryCaptor.capture());

        GetAllUserQuery query = queryCaptor.getValue();

        org.junit.jupiter.api.Assertions.assertAll(
                () -> org.junit.jupiter.api.Assertions.assertEquals(1, query.page()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(10, query.size()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(TypeUser.STUDENT, query.typeUser()),
                () -> org.junit.jupiter.api.Assertions.assertEquals("Student", query.name()),
                () -> org.junit.jupiter.api.Assertions.assertNotNull(query.excludeClassId())
        );
    }

    @Test
    void listReturnsBadRequestWhenPaginationIsInvalid() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A página deve ser maior ou igual a 0."));

        verifyNoInteractions(getAllUserUseCase);
    }

    @Test
    void listReturnsForbiddenWhenUserCannotListUsers() throws Exception {
        when(getAllUserUseCase.execute(any(GetAllUserQuery.class)))
                .thenThrow(new UserPermissionDeniedException("User does not have permission to list users."));

        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have permission to list users."));
    }

    @Test
    void deactivateReturns204WhenSuccessful() throws Exception {
        UUID userId = UUID.randomUUID();

        doNothing()
                .when(deactivateUserUseCase)
                .execute(any(DeactivateUserCommand.class));

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        ArgumentCaptor<DeactivateUserCommand> captor = ArgumentCaptor.forClass(DeactivateUserCommand.class);
        verify(deactivateUserUseCase).execute(captor.capture());

        org.junit.jupiter.api.Assertions.assertEquals(userId, captor.getValue().targetUserId());
    }

    @Test
    void deactivateReturns404WhenUserDoesNotExist() throws Exception {
        doThrow(new UserNotFoundException())
                .when(deactivateUserUseCase)
                .execute(any(DeactivateUserCommand.class));

        mockMvc.perform(delete("/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateReturns403WhenCallerLacksPermission() throws Exception {
        doThrow(new UserPermissionDeniedException("User does not have permission to deactivate this type of user."))
                .when(deactivateUserUseCase)
                .execute(any(DeactivateUserCommand.class));

        mockMvc.perform(delete("/users/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have permission to deactivate this type of user."));
    }

    @Test
    void getUserByIdReturnsUserWithoutSensitiveData() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-19T18:00:00Z");

        UserEntity user = new UserEntity(
                "Student Unit",
                "unit@estudante.sesisenai.org.br",
                "encoded-secret",
                TypeUser.STUDENT
        );
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "createdAt", createdAt);

        when(getUserByIdUseCase.execute(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Student Unit"))
                .andExpect(jsonPath("$.email").value("unit@estudante.sesisenai.org.br"))
                .andExpect(jsonPath("$.typeUser").value("STUDENT"))
                .andExpect(jsonPath("$.createdAt").value("2026-05-19T18:00:00Z"))
                .andExpect(jsonPath("$", not(hasKey("password"))));

        verify(getUserByIdUseCase).execute(userId);
    }

    @Test
    void getUserByIdReturns404WhenNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        when(getUserByIdUseCase.execute(userId))
                .thenThrow(new UserNotFoundException("User not found " + userId));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBulkUsersReturnsFoundAndMissingIds() throws Exception {
        UUID validId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-19T18:00:00Z");

        com.portal.conecta.hub.module.user.presentation.dto.response.UserResponse mappedUser =
                new com.portal.conecta.hub.module.user.presentation.dto.response.UserResponse(
                        validId, "Bulk Student", "bulk@senai.br", TypeUser.STUDENT, true, createdAt
                );

        com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse bulkResponse =
                new com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse(
                        List.of(mappedUser),
                        List.of(validId),
                        List.of(missingId)
                );

        when(getUsersBulkUseCase.execute(any(List.class))).thenReturn(bulkResponse);

        String jsonPayload = "{\"ids\": [\"%s\", \"%s\"]}".formatted(validId, missingId);

        mockMvc.perform(post("/users/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(validId.toString()))
                .andExpect(jsonPath("$.items[0].name").value("Bulk Student"))
                .andExpect(jsonPath("$.foundIds[0]").value(validId.toString()))
                .andExpect(jsonPath("$.missingIds[0]").value(missingId.toString()));

        verify(getUsersBulkUseCase).execute(any(List.class));
    }

    @Test
    void getBulkUsersReturns200WithEmptyItemsWhenNoIdsFound() throws Exception {
        UUID missingId1 = UUID.randomUUID();
        UUID missingId2 = UUID.randomUUID();

        com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse bulkResponse =
                new com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse(
                        List.of(),
                        List.of(),
                        List.of(missingId1, missingId2)
                );

        when(getUsersBulkUseCase.execute(any(List.class))).thenReturn(bulkResponse);

        String jsonPayload = "{\"ids\": [\"%s\", \"%s\"]}".formatted(missingId1, missingId2);

        mockMvc.perform(post("/users/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.foundIds").isEmpty())
                .andExpect(jsonPath("$.missingIds[0]").value(missingId1.toString()))
                .andExpect(jsonPath("$.missingIds[1]").value(missingId2.toString()));

        verify(getUsersBulkUseCase).execute(any(List.class));
    }

    @Test
    void getBulkUsersReturns400WhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/users/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\": [\"nao-sou-um-uuid\"]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/users/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getUsersBulkUseCase);
    }

    @Test
    void getBulkUsersReturnsAllFoundWithEmptyMissingIds() throws Exception {
        UUID validId1 = UUID.randomUUID();
        UUID validId2 = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-19T18:00:00Z");

        com.portal.conecta.hub.module.user.presentation.dto.response.UserResponse user1 =
                new com.portal.conecta.hub.module.user.presentation.dto.response.UserResponse(
                        validId1, "User One", "one@senai.br", TypeUser.STUDENT, true, createdAt
                );

        com.portal.conecta.hub.module.user.presentation.dto.response.UserResponse user2 =
                new com.portal.conecta.hub.module.user.presentation.dto.response.UserResponse(
                        validId2, "User Two", "two@senai.br", TypeUser.TEACHER, true, createdAt
                );

        com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse bulkResponse =
                new com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse(
                        List.of(user1, user2),
                        List.of(validId1, validId2),
                        List.of()
                );

        when(getUsersBulkUseCase.execute(any(List.class))).thenReturn(bulkResponse);

        String jsonPayload = "{\"ids\": [\"%s\", \"%s\"]}".formatted(validId1, validId2);

        mockMvc.perform(post("/users/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.foundIds.length()").value(2))
                .andExpect(jsonPath("$.missingIds").isEmpty());

        verify(getUsersBulkUseCase).execute(any(List.class));
    }

    @Test
    @DisplayName("GET /users/{userId}/class — deve retornar 200 com lista vazia quando o usuário existir mas não possuir turmas")
    void getActiveClassReturns200WithEmptyList() throws Exception {
        UUID userId = UUID.randomUUID();

        when(getActiveClassByUserUseCase.execute(any(GetActiveClassByUserCommand.class)))
                .thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/users/{userId}/class", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /users/{userId}/class — deve retornar 404 quando o usuário não existir ou estiver inativo")
    void getActiveClassReturns404WhenUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        when(getActiveClassByUserUseCase.execute(any(GetActiveClassByUserCommand.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/users/{userId}/class", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("controller não deve declarar campo de log próprio após remoção do @Slf4j")
    void userControllerShouldNotDeclareLogField() {
        boolean hasLogField = java.util.Arrays.stream(UserController.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("log"));

        org.junit.jupiter.api.Assertions.assertFalse(
                hasLogField,
                "UserController não deve declarar campo de log próprio"
        );
    }

}
