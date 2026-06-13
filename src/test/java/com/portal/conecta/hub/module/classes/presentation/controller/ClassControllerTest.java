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
                        reactivateClassUseCase
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

    private ClassEntity buildInactiveClass() {
        ClassEntity entity = buildActiveClass();
        UserEntity deletedBy = new UserEntity("Admin", "admin@test.com", "hash", TypeUser.SENAI);
        entity.delete(deletedBy);
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


    @Test
    @DisplayName("deve retornar 200 com turmas ativas por padrão")
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
    @DisplayName("deve retornar 401 quando não autenticado")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        when(getAllClassesUseCase.execute(any()))
                .thenThrow(new UnauthorizedUserException("Authentication is required."));

        mockMvc.perform(get("/classes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }


    @Test
    @DisplayName("deve retornar 201 ao adicionar membro")
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
    @DisplayName("deve retornar 200 ao promover representante")
    void shouldReturn200WhenPromoted() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(promoteToRepresentativeUseCase.execute(any()))
                .thenReturn(buildMembership(userId, classId, ClassRole.REPRESENTATIVE));

        mockMvc.perform(patch("/classes/{classId}/members/{userId}/representative",
                        classId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classRole").value("REPRESENTATIVE"));
    }


    @Test
    @DisplayName("deve retornar 204 ao deletar vínculo")
    void shouldReturn204WhenMembershipDeleted() throws Exception {
        mockMvc.perform(delete("/classes/{classId}/members/{userId}",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("deve retornar 200 no bulk")
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
}