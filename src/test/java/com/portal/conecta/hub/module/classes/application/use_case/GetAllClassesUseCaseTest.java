package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.query.ListClassesQuery;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllClassesUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private GetAllClassesUseCase useCase;

    @Test
    @DisplayName("deve retornar turmas ativas por padrão")
    void shouldReturnActiveClassesByDefault() {
        ClassEntity activeClass = mock(ClassEntity.class);
        Page<ClassEntity> page = new PageImpl<>(List.of(activeClass));

        when(classRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        ListClassesQuery query = new ListClassesQuery(0, 20, false, false);
        Page<ClassEntity> result = useCase.execute(query);

        assertThat(result.getContent()).hasSize(1);
        verify(classRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("deve retornar turmas ativas e inativas quando includeInactive=true")
    void shouldReturnAllClassesWhenIncludeInactive() {
        Page<ClassEntity> page = new PageImpl<>(List.of(mock(ClassEntity.class), mock(ClassEntity.class)));

        when(classRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        ListClassesQuery query = new ListClassesQuery(0, 20, true, false);
        Page<ClassEntity> result = useCase.execute(query);

        assertThat(result.getContent()).hasSize(2);
        verify(classRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("deve retornar apenas turmas inativas quando onlyInactive=true")
    void shouldReturnOnlyInactiveClassesWhenOnlyInactive() {
        ClassEntity inactiveClass = mock(ClassEntity.class);
        Page<ClassEntity> page = new PageImpl<>(List.of(inactiveClass));

        when(classRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        ListClassesQuery query = new ListClassesQuery(0, 20, false, true);
        Page<ClassEntity> result = useCase.execute(query);

        assertThat(result.getContent()).hasSize(1);
        verify(classRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("deve retornar página vazia quando não há turmas")
    void shouldReturnEmptyPageWhenNoClasses() {
        when(classRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(Page.empty());

        ListClassesQuery query = new ListClassesQuery(0, 20, false, false);
        Page<ClassEntity> result = useCase.execute(query);

        assertThat(result.getContent()).isEmpty();
        verify(classRepository).findAll(any(Specification.class), any(PageRequest.class));
    }
}