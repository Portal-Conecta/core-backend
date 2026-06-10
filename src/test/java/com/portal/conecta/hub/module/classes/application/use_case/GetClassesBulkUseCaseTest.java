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

        BulkClassResponse result = useCase.execute(List.of(id1, id2), false);

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

        BulkClassResponse result = useCase.execute(List.of(id1, id2), false);

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

        BulkClassResponse result = useCase.execute(List.of(id1, id2), false);

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

        BulkClassResponse result = useCase.execute(List.of(id1, id1), false);

        assertThat(result.items()).hasSize(1);
        assertThat(result.foundIds()).containsExactly(id1);
        assertThat(result.missingIds()).isEmpty();
        verify(classRepository).findAllByIdInAndDeletedAtIsNull(List.of(id1));
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando ids é nulo")
    void shouldThrowWhenIdsIsNull() {
        assertThatThrownBy(() -> useCase.execute(null, false))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(classRepository);
    }

    @Test
    @DisplayName("deve retornar turmas ativas e desativadas quando includeInactive é true")
    void shouldReturnActiveAndInactiveClasses_whenIncludeInactiveIsTrue() {
        UUID activeId   = UUID.randomUUID();
        UUID inactiveId = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");

        ClassEntity activeClass   = spy(buildClass("MIDS1", course));
        ClassEntity inactiveClass = spy(buildClass("MIDS2", course));
        doReturn(activeId).when(activeClass).getId();
        doReturn(inactiveId).when(inactiveClass).getId();

        when(classRepository.findAllByIdIn(List.of(activeId, inactiveId)))
                .thenReturn(List.of(activeClass, inactiveClass));

        BulkClassResponse result = useCase.execute(List.of(activeId, inactiveId), true);

        assertThat(result.foundIds()).containsExactlyInAnyOrder(activeId, inactiveId);
        assertThat(result.missingIds()).isEmpty();
        assertThat(result.items()).hasSize(2);
    }

    @Test
    @DisplayName("deve colocar ID inexistente em missingIds mesmo quando includeInactive é true")
    void shouldPutNonExistentInMissingIds_whenIncludeInactiveIsTrue() {
        UUID existingId = UUID.randomUUID();
        UUID missingId  = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");

        ClassEntity existing = spy(buildClass("MIDS1", course));
        doReturn(existingId).when(existing).getId();

        when(classRepository.findAllByIdIn(List.of(existingId, missingId)))
                .thenReturn(List.of(existing));

        BulkClassResponse result = useCase.execute(List.of(existingId, missingId), true);

        assertThat(result.foundIds()).containsExactly(existingId);
        assertThat(result.missingIds()).containsExactly(missingId);
    }

    @Test
    @DisplayName("deve usar findAllByIdIn quando includeInactive é true")
    void shouldCallFindAllByIdIn_whenIncludeInactiveIsTrue() {
        UUID id = UUID.randomUUID();

        when(classRepository.findAllByIdIn(List.of(id))).thenReturn(List.of());

        useCase.execute(List.of(id), true);

        verify(classRepository).findAllByIdIn(List.of(id));
        verify(classRepository, never()).findAllByIdInAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("deve usar findAllByIdInAndDeletedAtIsNull quando includeInactive é false")
    void shouldCallFindAllByIdInAndDeletedAtIsNull_whenIncludeInactiveIsFalse() {
        UUID id = UUID.randomUUID();

        when(classRepository.findAllByIdInAndDeletedAtIsNull(List.of(id))).thenReturn(List.of());

        useCase.execute(List.of(id), false);

        verify(classRepository).findAllByIdInAndDeletedAtIsNull(List.of(id));
        verify(classRepository, never()).findAllByIdIn(any());
    }
}