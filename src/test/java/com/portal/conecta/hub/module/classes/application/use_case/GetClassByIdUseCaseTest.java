package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
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
class GetClassByIdUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private GetClassByIdUseCase useCase;

    @Test
    @DisplayName("deve retornar turma ativa quando encontrada")
    void shouldReturnClassWhenFound() {
        UUID classId = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        ClassEntity classEntity = new ClassEntity(Shift.FULL_AM_PM, 1, "MIDS1", course);

        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.of(classEntity));

        ClassEntity result = useCase.execute(classId);

        assertThat(result).isEqualTo(classEntity);
        verify(classRepository).findByIdAndDeletedAtIsNull(classId);
    }

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma não existe")
    void shouldThrowWhenClassNotFound() {
        UUID classId = UUID.randomUUID();

        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(ClassEntityNotFoundException.class);

        verify(classRepository).findByIdAndDeletedAtIsNull(classId);
    }

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma está removida logicamente")
    void shouldThrowWhenClassIsDeleted() {
        UUID classId = UUID.randomUUID();

        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(ClassEntityNotFoundException.class);

        verify(classRepository).findByIdAndDeletedAtIsNull(classId);
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando classId é nulo")
    void shouldThrowWhenClassIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(classRepository);
    }
}