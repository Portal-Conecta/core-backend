package com.portal.conecta.hub.module.user.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.AddMemberCommand;
import com.portal.conecta.hub.module.classes.application.use_case.AddClassMemberUseCase;
import com.portal.conecta.hub.module.classes.application.use_case.CreateClassUseCase;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.presentation.controller.ClassController;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClassControllerTest {

    @Mock
    private CreateClassUseCase createClassUseCase;

    @Mock
    private AddClassMemberUseCase addClassMemberUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ClassController(createClassUseCase, addClassMemberUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- helper ---

    private ClassMembershipEntity buildMembership(UUID userId, UUID classId, ClassRole role) {
        UserEntity user = new UserEntity("User", "user@test.com", "hash", TypeUser.STUDENT);
        ReflectionTestUtils.setField(user, "id", userId);

        CourseEntity course = new CourseEntity("Curso", "CRS");
        UserEntity creator = new UserEntity("Creator", "creator@test.com", "hash", TypeUser.SENAI);
        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, creator);
        ReflectionTestUtils.setField(classEntity, "id", classId);

        return new ClassMembershipEntity(user, classEntity, role);
    }

    // --- 201 Created ---

    @Test
    @DisplayName("deve retornar 201 com dados do vínculo ao associar aluno")
    void shouldReturn201WhenStudentAdded() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any(AddMemberCommand.class)))
                .thenReturn(buildMembership(userId, classId, ClassRole.STUDENT));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.classId").value(classId.toString()))
                .andExpect(jsonPath("$.classRole").value("STUDENT"));
    }

    @Test
    @DisplayName("deve retornar 201 com dados do vínculo ao associar docente")
    void shouldReturn201WhenTeacherAdded() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any(AddMemberCommand.class)))
                .thenReturn(buildMembership(userId, classId, ClassRole.TEACHER));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "TEACHER"
                                }
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classRole").value("TEACHER"));
    }

    // --- 400 Bad Request ---

    @Test
    @DisplayName("deve retornar 400 quando role é REPRESENTATIVE")
    void shouldReturn400WhenRoleIsRepresentative() throws Exception {
        UUID classId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("REPRESENTATIVE não permitido"));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "REPRESENTATIVE"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("REPRESENTATIVE não permitido"));
    }

    @Test
    @DisplayName("deve retornar 400 quando usuário já está vinculado à turma")
    void shouldReturn400WhenMembershipAlreadyExists() throws Exception {
        UUID classId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("User already has an active membership in this class."));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 400 quando aluno já possui turma ativa")
    void shouldReturn400WhenStudentAlreadyHasActiveClass() throws Exception {
        UUID classId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("Student already has an active class."));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Student already has an active class."));
    }

    @Test
    @DisplayName("deve retornar 400 quando turma está removida")
    void shouldReturn400WhenClassIsDeleted() throws Exception {
        UUID classId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("Class is deleted and cannot receive new members."));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 400 quando usuário está removido ou inativo")
    void shouldReturn400WhenUserIsInactive() throws Exception {
        UUID classId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("User is inactive or deleted."));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- 401 Unauthorized ---

    @Test
    @DisplayName("deve retornar 401 quando requisição não está autenticada")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        UUID classId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new UnauthorizedUserException("Authentication is required."));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    // --- 403 Forbidden ---

    @Test
    @DisplayName("deve retornar 403 quando executor não tem permissão")
    void shouldReturn403WhenExecutorHasNoPermission() throws Exception {
        UUID classId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new UserPermissionDeniedException("Only ADMIN or SENAI can associate members to a class."));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only ADMIN or SENAI can associate members to a class."));
    }

    // --- 404 Not Found ---

    @Test
    @DisplayName("deve retornar 404 quando turma não existe")
    void shouldReturn404WhenClassNotFound() throws Exception {
        UUID classId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new ClassNotFoundException("Class not found: " + classId));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 404 quando usuário não existe")
    void shouldReturn404WhenUserNotFound() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
                .thenThrow(new UserNotFoundException("User not found: " + userId));

        mockMvc.perform(post("/classes/{classId}/members", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "classRole": "STUDENT"
                                }
                                """.formatted(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}