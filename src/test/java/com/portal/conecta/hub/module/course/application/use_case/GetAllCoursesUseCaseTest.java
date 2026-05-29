package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllCoursesUseCaseTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private GetAllCoursesUseCase useCase;

    @Test
    @DisplayName("deve retornar lista de cursos ativos")
    void shouldReturnActiveCourses() {
        CourseEntity course1 = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        CourseEntity course2 = new CourseEntity("Eletrotécnica", "ELT");

        when(courseRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(course1, course2));

        List<CourseEntity> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result).contains(course1, course2);
        verify(courseRepository).findAllByDeletedAtIsNull();
    }

    @Test
    @DisplayName("deve retornar lista vazia quando não há cursos ativos")
    void shouldReturnEmptyListWhenNoActiveCourses() {
        when(courseRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());

        List<CourseEntity> result = useCase.execute();

        assertThat(result).isEmpty();
        verify(courseRepository).findAllByDeletedAtIsNull();
    }
}