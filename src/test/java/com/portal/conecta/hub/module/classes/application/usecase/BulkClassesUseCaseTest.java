package com.portal.conecta.hub.module.classes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.presentation.dto.BulkClassesResponse;

@ExtendWith(MockitoExtension.class)
class BulkClassesUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private BulkClassesUseCase bulkClassesUseCase;

    @Test
    void shouldTreatInactiveAsMissingByDefault() {
        UUID activeId = UUID.randomUUID();
        UUID inactiveId = UUID.randomUUID();

        when(classRepository.findAllByIdIn(anyCollection()))
                .thenReturn(List.of(
                        new ClassEntity(activeId, true),
                        new ClassEntity(inactiveId, false)));

        BulkClassesResponse response = bulkClassesUseCase.execute(List.of(activeId, inactiveId), false);

        assertThat(response.foundIds()).containsExactly(activeId);
        assertThat(response.missingIds()).containsExactly(inactiveId);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().id()).isEqualTo(activeId);
    }

    @Test
    void shouldIncludeInactiveWhenExplicitlyRequested() {
        UUID activeId = UUID.randomUUID();
        UUID inactiveId = UUID.randomUUID();

        when(classRepository.findAllByIdIn(anyCollection()))
                .thenReturn(List.of(
                        new ClassEntity(activeId, true),
                        new ClassEntity(inactiveId, false)));

        BulkClassesResponse response = bulkClassesUseCase.execute(List.of(activeId, inactiveId), true);

        assertThat(response.foundIds()).containsExactlyInAnyOrder(activeId, inactiveId);
        assertThat(response.missingIds()).isEmpty();
        assertThat(response.items()).hasSize(2);
    }

    @Test
    void shouldKeepNonexistentIdsAsMissing() {
        UUID foundId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();

        when(classRepository.findAllByIdIn(anyCollection()))
                .thenReturn(List.of(new ClassEntity(foundId, true)));

        BulkClassesResponse response = bulkClassesUseCase.execute(List.of(foundId, missingId), false);

        assertThat(response.foundIds()).containsExactly(foundId);
        assertThat(response.missingIds()).containsExactly(missingId);
    }

    @Test
    void shouldNotDuplicateIdsInResponse() {
        UUID classId = UUID.randomUUID();

        when(classRepository.findAllByIdIn(anyCollection()))
                .thenReturn(List.of(new ClassEntity(classId, true)));

        BulkClassesResponse response = bulkClassesUseCase.execute(List.of(classId, classId, classId), false);

        assertThat(response.items()).hasSize(1);
        assertThat(response.foundIds()).containsExactly(classId);
        assertThat(response.missingIds()).isEmpty();
    }
}
