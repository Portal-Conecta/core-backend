package com.portal.conecta.hub.module.course.presentation.controller;

import com.portal.conecta.hub.module.course.application.command.CreateCourseCommand;
import com.portal.conecta.hub.module.course.application.command.UpdateCourseCommand;
import com.portal.conecta.hub.module.course.application.use_case.CreateCourseUseCase;
import com.portal.conecta.hub.module.course.application.use_case.GetAllCoursesUseCase;
import com.portal.conecta.hub.module.course.application.use_case.GetCourseByIdUseCase;
import com.portal.conecta.hub.module.course.application.use_case.UpdateCourseUseCase;
import com.portal.conecta.hub.module.course.domain.exception.CourseCodeAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.CourseEntityNotFoundException;
import com.portal.conecta.hub.module.course.domain.exception.CourseNameAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.DeletedCourseException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CreateCourseUseCase createCourseUseCase;

    @Mock
    private UpdateCourseUseCase updateCourseUseCase;

    @Mock
    private GetCourseByIdUseCase getCourseByIdUseCase;

    @Mock
    private GetAllCoursesUseCase getAllCoursesUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CourseController(createCourseUseCase, updateCourseUseCase, getAllCoursesUseCase, getCourseByIdUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("deve retornar 201 Created com dados do curso criado")
    void shouldReturnCreatedCourse() throws Exception {
        UUID courseId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-26T10:00:00Z");

        CourseEntity createdCourse = new CourseEntity("Desenvolvimento de Sistemas", "DS");
        ReflectionTestUtils.setField(createdCourse, "id", courseId);
        ReflectionTestUtils.setField(createdCourse, "createdAt", createdAt);
        ReflectionTestUtils.setField(createdCourse, "updatedAt", createdAt);

        when(createCourseUseCase.execute(any(CreateCourseCommand.class))).thenReturn(createdCourse);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Desenvolvimento de Sistemas",
                                  "code": "DS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/courses/" + courseId))
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.name").value("Desenvolvimento de Sistemas"))
                .andExpect(jsonPath("$.code").value("DS"))
                .andExpect(jsonPath("$.createdAt").value("2026-05-26T10:00:00Z"));

        ArgumentCaptor<CreateCourseCommand> captor = ArgumentCaptor.forClass(CreateCourseCommand.class);
        verify(createCourseUseCase).execute(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Desenvolvimento de Sistemas");
        assertThat(captor.getValue().code()).isEqualTo("DS");
    }

    @Test
    @DisplayName("create deve retornar 409 quando name já está em uso")
    void createShouldReturnConflictWhenNameAlreadyInUse() throws Exception {
        when(createCourseUseCase.execute(any(CreateCourseCommand.class)))
                .thenThrow(new CourseNameAlreadyInUseException("Name already in use: Desenvolvimento de Sistemas"));

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Desenvolvimento de Sistemas", "code": "DS"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Name already in use: Desenvolvimento de Sistemas"))
                .andExpect(jsonPath("$.path").value("/courses"));
    }

    @Test
    @DisplayName("create deve retornar 409 quando code já está em uso")
    void createShouldReturnConflictWhenCodeAlreadyInUse() throws Exception {
        when(createCourseUseCase.execute(any(CreateCourseCommand.class)))
                .thenThrow(new CourseCodeAlreadyInUseException("Code already in use: DS"));

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Desenvolvimento de Sistemas", "code": "DS"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Code already in use: DS"))
                .andExpect(jsonPath("$.path").value("/courses"));
    }

    @Test
    @DisplayName("create deve retornar 401 quando usuário não tem permissão")
    void createShouldReturnUnauthorizedWhenNoPermission() throws Exception {
        when(createCourseUseCase.execute(any(CreateCourseCommand.class)))
                .thenThrow(new UnauthorizedUserException());

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Desenvolvimento de Sistemas", "code": "DS"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/courses"));
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("deve retornar 200 OK com dados do curso atualizado")
    void shouldReturnUpdatedCourse() throws Exception {
        UUID courseId = UUID.randomUUID();
        Instant updatedAt = Instant.parse("2026-05-26T12:00:00Z");

        CourseEntity updatedCourse = new CourseEntity("Desenvolvimento de Sistemas", "DS");
        ReflectionTestUtils.setField(updatedCourse, "id", courseId);
        ReflectionTestUtils.setField(updatedCourse, "updatedAt", updatedAt);

        when(updateCourseUseCase.execute(any(UpdateCourseCommand.class))).thenReturn(updatedCourse);

        mockMvc.perform(patch("/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Desenvolvimento de Sistemas", "code": "DS"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.name").value("Desenvolvimento de Sistemas"))
                .andExpect(jsonPath("$.code").value("DS"))
                .andExpect(jsonPath("$.updatedAt").value("2026-05-26T12:00:00Z"));

        ArgumentCaptor<UpdateCourseCommand> captor = ArgumentCaptor.forClass(UpdateCourseCommand.class);
        verify(updateCourseUseCase).execute(captor.capture());
        assertThat(captor.getValue().courseId()).isEqualTo(courseId);
        assertThat(captor.getValue().name()).isEqualTo("Desenvolvimento de Sistemas");
        assertThat(captor.getValue().code()).isEqualTo("DS");
    }

    @Test
    @DisplayName("update deve retornar 200 OK com atualização parcial (só name)")
    void shouldReturnUpdatedCourseWithPartialUpdate() throws Exception {
        UUID courseId = UUID.randomUUID();
        Instant updatedAt = Instant.parse("2026-05-26T12:00:00Z");

        CourseEntity updatedCourse = new CourseEntity("Novo Nome", "DS");
        ReflectionTestUtils.setField(updatedCourse, "id", courseId);
        ReflectionTestUtils.setField(updatedCourse, "updatedAt", updatedAt);

        when(updateCourseUseCase.execute(any(UpdateCourseCommand.class))).thenReturn(updatedCourse);

        mockMvc.perform(patch("/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Novo Nome"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Novo Nome"));

        ArgumentCaptor<UpdateCourseCommand> captor = ArgumentCaptor.forClass(UpdateCourseCommand.class);
        verify(updateCourseUseCase).execute(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Novo Nome");
        assertThat(captor.getValue().code()).isNull();
    }

    @Test
    @DisplayName("update deve retornar 404 quando curso não existe")
    void updateShouldReturnNotFoundWhenCourseDoesNotExist() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(updateCourseUseCase.execute(any(UpdateCourseCommand.class)))
                .thenThrow(new CourseEntityNotFoundException("Course not found: " + courseId));

        mockMvc.perform(patch("/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Novo Nome"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("update deve retornar 404 quando curso está removido logicamente")
    void updateShouldReturnNotFouncdWhenCourseIsDeleted() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(updateCourseUseCase.execute(any(UpdateCourseCommand.class)))
                .thenThrow(new DeletedCourseException("Course is deleted: " + courseId));

        mockMvc.perform(patch("/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Novo Nome"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("update deve retornar 409 quando name já está em uso")
    void updateShouldReturnConflictWhenNameAlreadyInUse() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(updateCourseUseCase.execute(any(UpdateCourseCommand.class)))
                .thenThrow(new CourseNameAlreadyInUseException("Name already in use: Novo Nome"));

        mockMvc.perform(patch("/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Novo Nome"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Name already in use: Novo Nome"));
    }

    @Test
    @DisplayName("update deve retornar 409 quando code já está em uso")
    void updateShouldReturnConflictWhenCodeAlreadyInUse() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(updateCourseUseCase.execute(any(UpdateCourseCommand.class)))
                .thenThrow(new CourseCodeAlreadyInUseException("Code already in use: DS"));

        mockMvc.perform(patch("/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Novo Nome", "code": "DS"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Code already in use: DS"));
    }

    @Test
    @DisplayName("update deve retornar 401 quando usuário não tem permissão")
    void updateShouldReturnUnauthorizedWhenNoPermission() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(updateCourseUseCase.execute(any(UpdateCourseCommand.class)))
                .thenThrow(new UnauthorizedUserException());

        mockMvc.perform(patch("/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Novo Nome"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}