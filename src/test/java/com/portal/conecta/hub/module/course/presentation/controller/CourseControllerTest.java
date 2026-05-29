package com.portal.conecta.hub.module.course.presentation.controller;

import com.portal.conecta.hub.module.course.application.command.CreateCourseCommand;
import com.portal.conecta.hub.module.course.application.use_case.CreateCourseUseCase;
import com.portal.conecta.hub.module.course.application.use_case.GetAllCoursesUseCase;
import com.portal.conecta.hub.module.course.application.use_case.GetCourseByIdUseCase;
import com.portal.conecta.hub.module.course.domain.exception.CourseCodeAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.CourseNameAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CreateCourseUseCase createCourseUseCase;

    @Mock
    private GetCourseByIdUseCase getCourseByIdUseCase;

    @Mock
    private GetAllCoursesUseCase getAllCoursesUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CourseController(createCourseUseCase, getAllCoursesUseCase, getCourseByIdUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

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

        CreateCourseCommand command = captor.getValue();
        assertThat(command.name()).isEqualTo("Desenvolvimento de Sistemas");
        assertThat(command.code()).isEqualTo("DS");
    }

    @Test
    @DisplayName("deve retornar 409 Conflict quando name já está em uso")
    void shouldReturnConflictWhenNameAlreadyInUse() throws Exception {
        when(createCourseUseCase.execute(any(CreateCourseCommand.class)))
                .thenThrow(new CourseNameAlreadyInUseException("Name already in use: Desenvolvimento de Sistemas"));

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Desenvolvimento de Sistemas",
                                  "code": "DS"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Name already in use: Desenvolvimento de Sistemas"))
                .andExpect(jsonPath("$.path").value("/courses"));
    }

    @Test
    @DisplayName("deve retornar 409 Conflict quando code já está em uso")
    void shouldReturnConflictWhenCodeAlreadyInUse() throws Exception {
        when(createCourseUseCase.execute(any(CreateCourseCommand.class)))
                .thenThrow(new CourseCodeAlreadyInUseException("Code already in use: DS"));

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Desenvolvimento de Sistemas",
                                  "code": "DS"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Code already in use: DS"))
                .andExpect(jsonPath("$.path").value("/courses"));
    }

    @Test
    @DisplayName("deve retornar 403 Forbidden quando usuário não tem permissão")
    void shouldReturnForbiddenWhenUnauthorized() throws Exception {
        when(createCourseUseCase.execute(any(CreateCourseCommand.class)))
                .thenThrow(new com.portal.conecta.hub.shared.exception.UnauthorizedUserException());

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Desenvolvimento de Sistemas",
                                  "code": "DS"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/courses"));
    }
}