package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.use_case.classes.get.GetClassesBulkUseCase;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetClassesBulkUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private GetClassesBulkUseCase useCase;

    private ClassEntity buildActiveClass(
            String name,
            CourseEntity course
    ) {
        ClassEntity entity = spy(
                new ClassEntity(
                        Shift.FULL_AM_PM,
                        1,
                        name,
                        course
                )
        );

        doReturn(true).when(entity).isActive();

        return entity;
    }

    private ClassEntity buildInactiveClass(
            String name,
            CourseEntity course
    ) {
        ClassEntity entity = spy(
                new ClassEntity(
                        Shift.FULL_AM_PM,
                        1,
                        name,
                        course
                )
        );

        doReturn(false).when(entity).isActive();

        return entity;
    }

    @SuppressWarnings("unchecked")
    private Specification<ClassEntity> anySpecification() {
        return any(Specification.class);
    }

    @Test
    @DisplayName("deve retornar apenas turmas ativas quando todos os IDs são encontrados")
    void shouldReturnActiveClassesWhenAllFound() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        CourseEntity course = new CourseEntity(
                "Desenvolvimento de Sistemas",
                "MIDS"
        );

        ClassEntity class1 = buildActiveClass("MIDS1", course);
        ClassEntity class2 = buildActiveClass("MIDS2", course);

        doReturn(id1).when(class1).getId();
        doReturn(id2).when(class2).getId();

        when(classRepository.findAll(anySpecification()))
                .thenReturn(List.of(class1, class2));

        BulkClassResponse result =
                useCase.execute(List.of(id1, id2), false);

        assertThat(result.items()).hasSize(2);
        assertThat(result.foundIds())
                .containsExactlyInAnyOrder(id1, id2);
        assertThat(result.missingIds()).isEmpty();

        verify(classRepository).findAll(anySpecification());
    }

    @Test
    @DisplayName("deve separar encontrados e ausentes corretamente")
    void shouldSeparateFoundAndMissing() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        CourseEntity course = new CourseEntity(
                "Desenvolvimento de Sistemas",
                "MIDS"
        );

        ClassEntity class1 = buildActiveClass("MIDS1", course);

        doReturn(id1).when(class1).getId();

        when(classRepository.findAll(anySpecification()))
                .thenReturn(List.of(class1));

        BulkClassResponse result =
                useCase.execute(List.of(id1, id2), false);

        assertThat(result.items()).hasSize(1);
        assertThat(result.foundIds()).containsExactly(id1);
        assertThat(result.missingIds()).containsExactly(id2);
    }

    @Test
    @DisplayName("deve retornar todos os IDs como ausentes quando nenhum for encontrado")
    void shouldReturnAllAsMissingWhenNoneFound() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(classRepository.findAll(anySpecification()))
                .thenReturn(List.of());

        BulkClassResponse result =
                useCase.execute(List.of(id1, id2), false);

        assertThat(result.items()).isEmpty();
        assertThat(result.foundIds()).isEmpty();
        assertThat(result.missingIds())
                .containsExactlyInAnyOrder(id1, id2);
    }

    @Test
    @DisplayName("deve ignorar IDs duplicados na requisição")
    void shouldDeduplicateIds() {
        UUID id = UUID.randomUUID();

        CourseEntity course = new CourseEntity(
                "Desenvolvimento de Sistemas",
                "MIDS"
        );

        ClassEntity classEntity =
                buildActiveClass("MIDS1", course);

        doReturn(id).when(classEntity).getId();

        when(classRepository.findAll(anySpecification()))
                .thenReturn(List.of(classEntity));

        BulkClassResponse result =
                useCase.execute(List.of(id, id), false);

        assertThat(result.items()).hasSize(1);
        assertThat(result.foundIds()).containsExactly(id);
        assertThat(result.missingIds()).isEmpty();

        verify(classRepository, times(1))
                .findAll(anySpecification());
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando ids for nulo")
    void shouldThrowWhenIdsIsNull() {
        assertThatThrownBy(() -> useCase.execute(null, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Os identificadores das turmas são obrigatórios."
                );

        verifyNoInteractions(classRepository);
    }

    @Test
    @DisplayName("deve consultar o repository com Specification independentemente de includeInactive")
    void shouldAlwaysCallFindAllWithSpecification() {
        UUID id = UUID.randomUUID();

        when(classRepository.findAll(anySpecification()))
                .thenReturn(List.of());

        useCase.execute(List.of(id), false);
        useCase.execute(List.of(id), true);

        verify(classRepository, times(2))
                .findAll(anySpecification());

        verify(classRepository, never())
                .findAllByIdsNotDeleted(any());

        verify(classRepository, never())
                .findAllByIdIn(any());

        verify(classRepository, never())
                .findAllByIdInAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("deve retornar turmas ativas e inativas quando includeInactive for true")
    void shouldReturnActiveAndInactiveWhenIncludeInactiveIsTrue() {
        UUID activeId = UUID.randomUUID();
        UUID inactiveId = UUID.randomUUID();

        CourseEntity course = new CourseEntity(
                "Desenvolvimento de Sistemas",
                "MIDS"
        );

        ClassEntity activeClass =
                buildActiveClass("MIDS1", course);

        ClassEntity inactiveClass =
                buildInactiveClass("MIDS2", course);

        doReturn(activeId).when(activeClass).getId();
        doReturn(inactiveId).when(inactiveClass).getId();

        /*
         * A Specification com includeInactive=true retornaria
         * tanto a turma ativa quanto a inativa.
         */
        when(classRepository.findAll(anySpecification()))
                .thenReturn(List.of(activeClass, inactiveClass));

        BulkClassResponse result =
                useCase.execute(
                        List.of(activeId, inactiveId),
                        true
                );

        assertThat(result.foundIds())
                .containsExactlyInAnyOrder(activeId, inactiveId);

        assertThat(result.missingIds()).isEmpty();
        assertThat(result.items()).hasSize(2);
    }

    @Test
    @DisplayName("deve colocar turma deletada em missingIds mesmo quando includeInactive for true")
    void shouldPutDeletedClassInMissingIdsWhenIncludeInactiveIsTrue() {
        UUID existingId = UUID.randomUUID();
        UUID deletedId = UUID.randomUUID();

        CourseEntity course = new CourseEntity(
                "Desenvolvimento de Sistemas",
                "MIDS"
        );

        ClassEntity existing =
                buildActiveClass("MIDS1", course);

        doReturn(existingId).when(existing).getId();

        /*
         * A Specification contém isNotDeleted(), portanto
         * a turma deletada não seria retornada pelo repository.
         */
        when(classRepository.findAll(anySpecification()))
                .thenReturn(List.of(existing));

        BulkClassResponse result =
                useCase.execute(
                        List.of(existingId, deletedId),
                        true
                );

        assertThat(result.foundIds())
                .containsExactly(existingId);

        assertThat(result.missingIds())
                .containsExactly(deletedId);

        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("não deve retornar turmas inativas quando includeInactive for false")
    void shouldNotReturnInactiveClassesWhenIncludeInactiveIsFalse() {
        UUID activeId = UUID.randomUUID();
        UUID inactiveId = UUID.randomUUID();

        CourseEntity course = new CourseEntity(
                "Desenvolvimento de Sistemas",
                "MIDS"
        );

        ClassEntity activeClass =
                buildActiveClass("MIDS1", course);

        doReturn(activeId).when(activeClass).getId();

        /*
         * O mock não executa a Specification.
         *
         * Como includeInactive=false, simulamos o resultado
         * que o banco retornaria: somente a turma ativa.
         */
        when(classRepository.findAll(anySpecification()))
                .thenReturn(List.of(activeClass));

        BulkClassResponse result =
                useCase.execute(
                        List.of(activeId, inactiveId),
                        false
                );

        assertThat(result.foundIds())
                .containsExactly(activeId);

        assertThat(result.missingIds())
                .containsExactly(inactiveId);

        assertThat(result.items()).hasSize(1);
    }
}