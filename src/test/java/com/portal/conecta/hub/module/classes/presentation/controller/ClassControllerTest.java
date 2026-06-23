package com.portal.conecta.hub.module.classes.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.*;
import com.portal.conecta.hub.module.classes.application.use_case.*;
import com.portal.conecta.hub.module.classes.domain.exception.*;
import com.portal.conecta.hub.module.classes.domain.model.*;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.exception.*;
import com.portal.conecta.hub.module.user.domain.model.*;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClassControllerTest {

    @Mock private CreateClassUseCase createClassUseCase;
    @Mock private AddClassMemberUseCase addClassMemberUseCase;
    @Mock private DeleteClassUseCase deleteClassUseCase;
    @Mock private PromoteToRepresentativeUseCase promoteToRepresentativeUseCase;
    @Mock private DemoteFromRepresentativeUseCase demoteFromRepresentativeUseCase;
    @Mock private DeleteClassMembershipUseCase deleteClassMembershipUseCase;
    @Mock private GetClassByIdUseCase getClassByIdUseCase;
    @Mock private GetClassesBulkUseCase getClassesBulkUseCase;
    @Mock private GetAllClassesUseCase getAllClassesUseCase;
    @Mock private RestoreClassUseCase restoreClassUseCase;
    @Mock private DeactivateClassUseCase deactivateClassUseCase;
    @Mock private ReactivateClassUseCase reactivateClassUseCase;
    @Mock private GetClassStudentUseCase getClassStudentsUseCase;
    @Mock private BulkAddClassMembersUseCase bulkAddClassMembersUseCase;
    @Mock private  GetActiveClassByUserUseCase getActiveClassByUserUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ClassController(
                        createClassUseCase,
                        deleteClassUseCase,
                        addClassMemberUseCase,
                        promoteToRepresentativeUseCase,
                        demoteFromRepresentativeUseCase,
                        deleteClassMembershipUseCase,
                        getClassByIdUseCase,
                        getClassesBulkUseCase,
                        getAllClassesUseCase,
                        restoreClassUseCase,
                        deactivateClassUseCase,
                        reactivateClassUseCase,
                        getClassStudentsUseCase,
                        bulkAddClassMembersUseCase,
                        getActiveClassByUserUseCase
                ))
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ClassEntity buildActiveClass() {
        CourseEntity course = new CourseEntity("DS", "DS");
        UserEntity creator = new UserEntity("Creator", "creator@test.com", "hash", TypeUser.SENAI);
        ClassEntity entity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, creator);
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        return entity;
    }

    private ClassMembershipEntity buildMembership(UUID userId, UUID classId, ClassRole role) {
        UserEntity user = new UserEntity("User", "user@test.com", "hash", TypeUser.STUDENT);
        ReflectionTestUtils.setField(user, "id", userId);

        CourseEntity course = new CourseEntity("Curso", "CRS");
        UserEntity creator = new UserEntity("Creator", "creator@test.com", "hash", TypeUser.SENAI);
        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, creator);
        ReflectionTestUtils.setField(classEntity, "id", classId);

        return new ClassMembershipEntity(user, classEntity, role);
    }

    private ClassMembershipEntity buildStudentMembership(String name, String email, TypeUser typeUser, ClassRole role) {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        UserEntity user = new UserEntity(name, email, "hash", typeUser);
        ReflectionTestUtils.setField(user, "id", userId);

        CourseEntity course = new CourseEntity("DS", "DS");
        UserEntity creator = new UserEntity("Creator", "creator@sc.senai.br", "hash", TypeUser.SENAI);
        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, creator);
        ReflectionTestUtils.setField(classEntity, "id", classId);

        return new ClassMembershipEntity(user, classEntity, role);
    }


    @Test
    @DisplayName("GET /classes — deve retornar 200 com turmas ativas por padrão")
    void shouldReturn200WithActiveClassesByDefault() throws Exception {
        Page<ClassEntity> page = new PageImpl<>(
                List.of(buildActiveClass()),
                PageRequest.of(0, 20),
                1
        );

        when(getAllClassesUseCase.execute(any())).thenReturn(page);

        mockMvc.perform(get("/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].active").value(true));
    }

    @Test
    @DisplayName("GET /classes — deve retornar 401 quando não autenticado")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        when(getAllClassesUseCase.execute(any()))
                .thenThrow(new UnauthorizedUserException("Authentication is required."));

        mockMvc.perform(get("/classes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }


    @Test
    @DisplayName("POST /classes/{classId}/members — deve retornar 201 ao adicionar membro")
    void shouldReturn201WhenMemberAdded() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(addClassMemberUseCase.execute(any()))
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
                .andExpect(jsonPath("$.classRole").value("STUDENT"));
    }



    @Test
    @DisplayName("PATCH /classes/{classId}/members/{userId}/representative — deve retornar 200 ao promover representante")
    void shouldReturn200WhenPromoted() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(promoteToRepresentativeUseCase.execute(any()))
                .thenReturn(buildMembership(userId, classId, ClassRole.REPRESENTATIVE));

        mockMvc.perform(patch("/classes/{classId}/members/{userId}/representative", classId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classRole").value("REPRESENTATIVE"));
    }



    @Test
    @DisplayName("DELETE /classes/{classId}/members/{userId} — deve retornar 204 ao remover vínculo")
    void shouldReturn204WhenMembershipDeleted() throws Exception {
        mockMvc.perform(delete("/classes/{classId}/members/{userId}",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("POST /classes/bulk — deve retornar 200 com resultado do bulk")
    void shouldReturn200ForBulk() throws Exception {
        UUID id = UUID.randomUUID();

        when(getClassesBulkUseCase.execute(List.of(id), false))
                .thenReturn(new com.portal.conecta.hub.module.classes.presentation.dto.response.BulkClassResponse(
                        List.of(), List.of(id), List.of()
                ));

        mockMvc.perform(post("/classes/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "ids": ["%s"]
                            }
                            """.formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foundIds[0]").value(id.toString()));
    }



    @Test
    @DisplayName("GET /classes/{classId}/students — deve retornar 200 com alunos e representantes")
    void shouldReturn200WithStudentsAndRepresentatives() throws Exception {
        UUID classId = UUID.randomUUID();

        ClassMembershipEntity student = buildStudentMembership(
                "Aluno Teste", "aluno@estudante.sesisenai.org.br", TypeUser.STUDENT, ClassRole.STUDENT);
        ClassMembershipEntity rep = buildStudentMembership(
                "Representante Teste", "rep@estudante.sesisenai.org.br", TypeUser.REPRESENTATIVE, ClassRole.REPRESENTATIVE);

        when(getClassStudentsUseCase.execute(classId)).thenReturn(List.of(student, rep));

        mockMvc.perform(get("/classes/{classId}/students", classId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Aluno Teste"))
                .andExpect(jsonPath("$[1].name").value("Representante Teste"));
    }

    @Test
    @DisplayName("GET /classes/{classId}/students — deve retornar 200 com lista vazia quando não há alunos")
    void shouldReturn200WithEmptyListWhenNoStudents() throws Exception {
        UUID classId = UUID.randomUUID();

        when(getClassStudentsUseCase.execute(classId)).thenReturn(List.of());

        mockMvc.perform(get("/classes/{classId}/students", classId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /classes/{classId}/students — deve retornar 404 quando turma não existe ou está desativada")
    void shouldReturn404WhenClassNotFoundForStudents() throws Exception {
        UUID classId = UUID.randomUUID();

        when(getClassStudentsUseCase.execute(classId))
                .thenThrow(ClassEntityNotFoundException.class);

        mockMvc.perform(get("/classes/{classId}/students", classId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /classes/{classId}/students — não deve expor dados sensíveis do usuário")
    void shouldNotExposeSensitiveDataInStudentsResponse() throws Exception {
        UUID classId = UUID.randomUUID();

        ClassMembershipEntity student = buildStudentMembership(
                "Aluno Teste", "aluno@estudante.sesisenai.org.br", TypeUser.STUDENT, ClassRole.STUDENT);

        when(getClassStudentsUseCase.execute(classId)).thenReturn(List.of(student));

        mockMvc.perform(get("/classes/{classId}/students", classId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[0].type").doesNotExist())
                .andExpect(jsonPath("$[0].deletedAt").doesNotExist());
    }

    @Test
    @DisplayName("GET /classes/{classId}/students — deve retornar 401 quando não autenticado")
    void shouldReturn401WhenNotAuthenticatedForStudents() throws Exception {
        UUID classId = UUID.randomUUID();

        when(getClassStudentsUseCase.execute(classId))
                .thenThrow(new UnauthorizedUserException("Authentication is required."));

        mockMvc.perform(get("/classes/{classId}/students", classId))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("POST /classes/{classId}/members/bulk — deve retornar 201 com todos os vínculos criados")
    void shouldReturn201WhenBulkMembersAdded() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userIdA = UUID.randomUUID();
        UUID userIdB = UUID.randomUUID();

        ClassMembershipEntity memberA = buildMembership(userIdA, classId, ClassRole.STUDENT);
        ClassMembershipEntity memberB = buildMembership(userIdB, classId, ClassRole.STUDENT);

        when(bulkAddClassMembersUseCase.execute(any())).thenReturn(List.of(memberA, memberB));

        mockMvc.perform(post("/classes/{classId}/members/bulk", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "members": [
                                    { "userId": "%s", "classRole": "STUDENT" },
                                    { "userId": "%s", "classRole": "STUDENT" }
                                  ]
                                }
                                """.formatted(userIdA, userIdB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].classRole").value("STUDENT"))
                .andExpect(jsonPath("$.items[1].classRole").value("STUDENT"));
    }

    @Test
    @DisplayName("POST /classes/{classId}/members/bulk — deve retornar 400 quando lista de membros está vazia")
    void shouldReturn400WhenBulkMembersListIsEmpty() throws Exception {
        UUID classId = UUID.randomUUID();

        mockMvc.perform(post("/classes/{classId}/members/bulk", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "members": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /classes/{classId}/members/bulk — deve retornar 400 quando item da lista não tem userId")
    void shouldReturn400WhenBulkItemMissingUserId() throws Exception {
        UUID classId = UUID.randomUUID();

        mockMvc.perform(post("/classes/{classId}/members/bulk", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "members": [
                                    { "classRole": "STUDENT" }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /classes/{classId}/members/bulk — deve retornar 400 quando item da lista não tem classRole")
    void shouldReturn400WhenBulkItemMissingClassRole() throws Exception {
        UUID classId = UUID.randomUUID();

        mockMvc.perform(post("/classes/{classId}/members/bulk", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "members": [
                                    { "userId": "%s" }
                                  ]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /classes/{classId}/members/bulk — deve retornar 400 quando regra de vínculo é violada")
    void shouldReturn400WhenBulkViolatesBusinessRule() throws Exception {
        UUID classId = UUID.randomUUID();

        when(bulkAddClassMembersUseCase.execute(any()))
                .thenThrow(new ClassMembershipException("O usuário já possui uma matrícula ativa nesta turma."));

        mockMvc.perform(post("/classes/{classId}/members/bulk", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "members": [
                                    { "userId": "%s", "classRole": "STUDENT" }
                                  ]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /classes/{classId}/members/bulk — deve retornar 403 quando executor não tem permissão")
    void shouldReturn403WhenBulkExecutorHasNoPermission() throws Exception {
        UUID classId = UUID.randomUUID();

        when(bulkAddClassMembersUseCase.execute(any()))
                .thenThrow(new UserPermissionDeniedException("Apenas ADMIN ou SENAI podem associar membros a uma turma."));

        mockMvc.perform(post("/classes/{classId}/members/bulk", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "members": [
                                    { "userId": "%s", "classRole": "STUDENT" }
                                  ]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /classes/{classId}/members/bulk — deve retornar 404 quando turma não existe")
    void shouldReturn404WhenBulkClassNotFound() throws Exception {
        UUID classId = UUID.randomUUID();

        when(bulkAddClassMembersUseCase.execute(any()))
                .thenThrow(ClassEntityNotFoundException.class);

        mockMvc.perform(post("/classes/{classId}/members/bulk", classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "members": [
                                    { "userId": "%s", "classRole": "STUDENT" }
                                  ]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /classes/users/{userId} — deve retornar 200 com as turmas ativas do usuário")
    void shouldReturn200WithActiveClassMembershipsWhenFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        ClassMembershipEntity membership = buildMembership(userId, classId, ClassRole.STUDENT);

        when(getActiveClassByUserUseCase.execute(any(GetActiveClassByUserCommand.class)))
                .thenReturn(List.of(membership));

        mockMvc.perform(get("/classes/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(classId.toString()))
                .andExpect(jsonPath("$[0].classRole").value("STUDENT"));
    }

    @Test
    @DisplayName("POST /classes — deve retornar 201 ao criar turma com número informado")
    void shouldReturn201WhenClassCreatedWithValidNumber() throws Exception {
        ClassEntity classEntity = buildActiveClass();

        when(createClassUseCase.execute(any())).thenReturn(classEntity);

        mockMvc.perform(post("/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "courseId": "%s",
                              "number": 78,
                              "shift": "FULL_AM_PM"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /classes — deve retornar 400 quando number está ausente")
    void shouldReturn400WhenNumberIsAbsent() throws Exception {
        mockMvc.perform(post("/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "courseId": "%s",
                              "shift": "FULL_AM_PM"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /classes — deve retornar 400 quando number é zero")
    void shouldReturn400WhenNumberIsZero() throws Exception {
        mockMvc.perform(post("/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "courseId": "%s",
                              "number": 0,
                              "shift": "FULL_AM_PM"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /classes — deve retornar 400 quando number é negativo")
    void shouldReturn400WhenNumberIsNegative() throws Exception {
        mockMvc.perform(post("/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "courseId": "%s",
                              "number": -5,
                              "shift": "FULL_AM_PM"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /classes — deve retornar 409 quando número já existe no mesmo curso")
    void shouldReturn409WhenNumberAlreadyExistsForSameCourse() throws Exception {
        when(createClassUseCase.execute(any()))
                .thenThrow(new ClassNumberAlreadyInUseException(78));

        mockMvc.perform(post("/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "courseId": "%s",
                              "number": 78,
                              "shift": "FULL_AM_PM"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isConflict());
    }
}