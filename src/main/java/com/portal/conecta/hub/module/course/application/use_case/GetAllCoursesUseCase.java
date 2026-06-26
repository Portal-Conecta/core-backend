package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Caso de uso para listagem completa do catálogo de cursos.
 * <p>
 * Retorna apenas cursos ativos, ignorando automaticamente todos os registros
 * que possuem data de exclusão preenchida.
 */
@Component
public class GetAllCoursesUseCase {

    private final CourseRepository courseRepository;

    public GetAllCoursesUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<CourseEntity> execute (){
        return courseRepository.findAllByDeletedAtIsNull();
    }
}
