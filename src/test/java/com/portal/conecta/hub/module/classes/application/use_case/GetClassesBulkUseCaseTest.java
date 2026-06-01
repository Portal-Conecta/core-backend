package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.presentation.dto.response.BulkClassResponse;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetClassesBulkUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private GetClassesBulkUseCase useCase;

    private ClassEntity buildClass(String name, CourseEntity course) {
        return new ClassEntity(Shift.FULL_AM_PM, 1, name, course);
    }

    @Test
    @DisplayName("deve retornar todas as turmas quando todos os IDs são encontrados")
    void shouldReturnAllWhenAllFound() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        ClassEntity class1 = spy(buildClass("MIDS1", course));
        ClassEntity class2 = spy(buildClass("MIDS2", course));
        doReturn(id1).when(class1).getId();
        doReturn(id2).when(class2).getId();

        when(classRepository.findAllByIdInAndDeletedAtIsNull(List.of(id1, id2)))
                .thenReturn(List.of(class1, class2));

        BulkClassResponse result = useCase.execute(List.of(id1, id2));

        assertThat(result.items()).hasSize(2);
        assertThat(result.foundIds()).containsExactlyInAnyOrder(id1, id2);
        assertThat(result.missingIds()).isEmpty();
    }

    @Test
    @DisplayName("deve separar encontrados e ausentes corretamente")
    void shouldSeparateFoundAndMissing() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        ClassEntity class1 = spy(buildClass("MIDS1", course));
        doReturn(id1).when(class1).getId();

        when(classRepository.findAllByIdInAndDeletedAtIsNull(List.of(id1, id2)))
                .thenReturn(List.of(class1));

        BulkClassResponse result = useCase.execute(List.of(id1, id2));

        assertThat(result.items()).hasSize(1);
        assertThat(result.foundIds()).containsExactly(id1);
        assertThat(result.missingIds()).containsExactly(id2);
    }

    @Test
    @DisplayName("deve retornar tudo em missingIds quando nenhum ID é encontrado")
    void shouldReturnAllAsMissingWhenNoneFound() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(classRepository.findAllByIdInAndDeletedAtIsNull(List.of(id1, id2)))
                .thenReturn(List.of());

        BulkClassResponse result = useCase.execute(List.of(id1, id2));

        assertThat(result.items()).isEmpty();
        assertThat(result.foundIds()).isEmpty();
        assertThat(result.missingIds()).containsExactlyInAnyOrder(id1, id2);
    }

    @Test
    @DisplayName("deve ignorar IDs duplicados na requisição")
    void shouldDeduplicateIds() {
        UUID id1 = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        ClassEntity class1 = spy(buildClass("MIDS1", course));
        doReturn(id1).when(class1).getId();

        when(classRepository.findAllByIdInAndDeletedAtIsNull(List.of(id1)))
                .thenReturn(List.of(class1));

        BulkClassResponse result = useCase.execute(List.of(id1, id1));

        assertThat(result.items()).hasSize(1);
        assertThat(result.foundIds()).containsExactly(id1);
        assertThat(result.missingIds()).isEmpty();
        verify(classRepository).findAllByIdInAndDeletedAtIsNull(List.of(id1));
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando ids é nulo")
    void shouldThrowWhenIdsIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(classRepository);
    }
}