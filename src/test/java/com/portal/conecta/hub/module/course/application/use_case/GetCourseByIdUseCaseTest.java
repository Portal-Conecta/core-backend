package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCourseByIdUseCaseTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private GetCourseByIdUseCase useCase;

    @Test
    @DisplayName("deve retornar curso ativo quando encontrado")
    void shouldReturnCourseWhenFound() {
        UUID courseId = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");

        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course));

        CourseEntity result = useCase.execute(courseId);

        assertThat(result).isEqualTo(course);
        verify(courseRepository).findByIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("deve lançar CourseNotFoundException quando curso não existe")
    void shouldThrowWhenCourseNotFound() {
        UUID courseId = UUID.randomUUID();

        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(courseId))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(courseId.toString());

        verify(courseRepository).findByIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("deve lançar CourseNotFoundException quando curso está removido logicamente")
    void shouldThrowWhenCourseIsDeleted() {
        UUID courseId = UUID.randomUUID();

        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(courseId))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(courseId.toString());

        verify(courseRepository).findByIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando courseId é nulo")
    void shouldThrowWhenCourseIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(courseRepository);
    }
}