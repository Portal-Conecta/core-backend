package com.portal.conecta.hub.module.classes.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.AddMemberCommand;
import com.portal.conecta.hub.module.classes.application.command.DeleteMembershipCommand;
import com.portal.conecta.hub.module.classes.application.command.DemoteMemberCommand;
import com.portal.conecta.hub.module.classes.application.command.PromoteMemberCommand;
import com.portal.conecta.hub.module.classes.application.use_case.*;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClassControllerTest {

    @Mock
    private CreateClassUseCase createClassUseCase;

    @Mock
    private AddClassMemberUseCase addClassMemberUseCase;

    @Mock
    private DeleteClassUseCase deleteClassUseCase;

    @Mock
    private PromoteToRepresentativeUseCase promoteToRepresentativeUseCase;

    @Mock
    private DemoteFromRepresentativeUseCase demoteFromRepresentativeUseCase;

    @Mock
    private DeleteClassMembershipUseCase deleteClassMembershipUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ClassController(createClassUseCase,deleteClassUseCase,addClassMemberUseCase,promoteToRepresentativeUseCase, demoteFromRepresentativeUseCase, deleteClassMembershipUseCase))
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
                .thenThrow(new ClassEntityNotFoundException("Class not found: " + classId));

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

    // --- PATCH /classes/{classId}/members/{userId}/representative ---

    @Test
    @DisplayName("deve retornar 200 com dados do vínculo promovido")
    void shouldReturn200WhenPromotedToRepresentative() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity("Student", "student@test.com", "hash", TypeUser.REPRESENTATIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        CourseEntity course = new CourseEntity("Curso", "CRS");
        UserEntity creator = new UserEntity("Creator", "creator@test.com", "hash", TypeUser.SENAI);
        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, creator);
        ReflectionTestUtils.setField(classEntity, "id", classId);
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.REPRESENTATIVE);

        when(promoteToRepresentativeUseCase.execute(any(PromoteMemberCommand.class)))
                .thenReturn(membership);

        mockMvc.perform(patch("/classes/{classId}/members/{userId}/representative", classId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.classId").value(classId.toString()))
                .andExpect(jsonPath("$.classRole").value("REPRESENTATIVE"))
                .andExpect(jsonPath("$.userType").value("REPRESENTATIVE"));
    }

    @Test
    @DisplayName("deve retornar 403 quando executor não tem permissão para promover")
    void shouldReturn403WhenExecutorCannotPromote() throws Exception {
        when(promoteToRepresentativeUseCase.execute(any()))
                .thenThrow(new UserPermissionDeniedException("Only ADMIN or SENAI can promote members to representative."));

        mockMvc.perform(patch("/classes/{classId}/members/{userId}/representative",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 400 quando usuário não tem vínculo com a turma")
    void shouldReturn400WhenNoMembership() throws Exception {
        when(promoteToRepresentativeUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("User does not have an active membership in this class."));

        mockMvc.perform(patch("/classes/{classId}/members/{userId}/representative",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 400 quando turma já atingiu o limite de representantes")
    void shouldReturn400WhenRepresentativeLimitReached() throws Exception {
        when(promoteToRepresentativeUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("Class already has the maximum number of active representatives."));

        mockMvc.perform(patch("/classes/{classId}/members/{userId}/representative",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 404 quando turma não existe na promoção")
    void shouldReturn404WhenClassNotFoundForPromotion() throws Exception {
        UUID classId = UUID.randomUUID();

        when(promoteToRepresentativeUseCase.execute(any()))
                .thenThrow(new ClassEntityNotFoundException("Class not found: " + classId));

        mockMvc.perform(patch("/classes/{classId}/members/{userId}/representative",
                        classId, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 404 quando usuário não existe na promoção")
    void shouldReturn404WhenUserNotFoundForPromotion() throws Exception {
        UUID userId = UUID.randomUUID();

        when(promoteToRepresentativeUseCase.execute(any()))
                .thenThrow(new UserNotFoundException("User not found: " + userId));

        mockMvc.perform(patch("/classes/{classId}/members/{userId}/representative",
                        UUID.randomUUID(), userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- DELETE /classes/{classId}/members/{userId}/representative ---

    @Test
    @DisplayName("deve retornar 200 com dados do vínculo rebaixado para STUDENT")
    void shouldReturn200WhenDemotedToStudent() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity("Student", "student@test.com", "hash", TypeUser.STUDENT);
        ReflectionTestUtils.setField(user, "id", userId);
        CourseEntity course = new CourseEntity("Curso", "CRS");
        UserEntity creator = new UserEntity("Creator", "creator@test.com", "hash", TypeUser.SENAI);
        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, creator);
        ReflectionTestUtils.setField(classEntity, "id", classId);
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, ClassRole.STUDENT);

        when(demoteFromRepresentativeUseCase.execute(any(DemoteMemberCommand.class)))
                .thenReturn(membership);

        mockMvc.perform(delete("/classes/{classId}/members/{userId}/representative", classId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.classId").value(classId.toString()))
                .andExpect(jsonPath("$.classRole").value("STUDENT"))
                .andExpect(jsonPath("$.userType").value("STUDENT"));
    }

    @Test
    @DisplayName("deve retornar 403 quando executor não tem permissão para remover representante")
    void shouldReturn403WhenExecutorCannotDemote() throws Exception {
        when(demoteFromRepresentativeUseCase.execute(any()))
                .thenThrow(new UserPermissionDeniedException("Only ADMIN or SENAI can remove a representative."));

        mockMvc.perform(delete("/classes/{classId}/members/{userId}/representative",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 400 quando o vínculo não existe ao tentar remover representante")
    void shouldReturn400WhenMembershipNotFoundForDemotion() throws Exception {
        when(demoteFromRepresentativeUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("User does not have an active membership in this class."));

        mockMvc.perform(delete("/classes/{classId}/members/{userId}/representative",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 400 quando o vínculo não é de representante ou está inativo")
    void shouldReturn400WhenInvalidDemotionState() throws Exception {
        when(demoteFromRepresentativeUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("Only memberships with role REPRESENTATIVE can be demoted."));

        mockMvc.perform(delete("/classes/{classId}/members/{userId}/representative",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 404 quando o executor não for encontrado na base")
    void shouldReturn404WhenExecutorNotFoundForDemotion() throws Exception {
        when(demoteFromRepresentativeUseCase.execute(any()))
                .thenThrow(new UserNotFoundException("Executor not found"));

        mockMvc.perform(delete("/classes/{classId}/members/{userId}/representative",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- DELETE /classes/{classId}/members/{userId} (Hard Delete) ---

    @Test
    @DisplayName("deve retornar 204 No Content ao deletar vínculo com sucesso")
    void shouldReturn204WhenMembershipDeleted() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Como o endpoint retorna void, apenas fazemos o mockMvc aguardar sucesso (isNoContent)
        // doNothing() é o comportamento padrão do mockito para métodos void, então não precisamos stubar o mock explicitamente aqui.

        mockMvc.perform(delete("/classes/{classId}/members/{userId}", classId, userId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist()); // Corpo vazio
    }

    @Test
    @DisplayName("deve retornar 403 quando executor não tem permissão para deletar vínculo")
    void shouldReturn403WhenExecutorCannotDeleteMembership() throws Exception {
        doThrow(new UserPermissionDeniedException("Only ADMIN or SENAI can remove class memberships."))
                .when(deleteClassMembershipUseCase).execute(any(DeleteMembershipCommand.class));

        mockMvc.perform(delete("/classes/{classId}/members/{userId}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 404 Not Found quando o vínculo não existe ao tentar deletar")
    void shouldReturn404WhenMembershipNotFoundForDeletion() throws Exception {
        doThrow(new ClassMembershipNotFoundException("Membership not found."))
                .when(deleteClassMembershipUseCase).execute(any(DeleteMembershipCommand.class));

        mockMvc.perform(delete("/classes/{classId}/members/{userId}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNotFound()) // Garante o 404
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve retornar 400 quando o usuário tenta deletar o próprio vínculo")
    void shouldReturn400WhenUserTriesToDeleteOwnMembership() throws Exception {
        doThrow(new ClassMembershipException("User cannot remove their own membership."))
                .when(deleteClassMembershipUseCase).execute(any(DeleteMembershipCommand.class));

        mockMvc.perform(delete("/classes/{classId}/members/{userId}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}