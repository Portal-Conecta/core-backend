package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso para consulta de um curso específico pelo seu identificador.
 * <p>
 * Retorna apenas cursos ativos. Registros que sofreram exclusão lógica
 * (soft delete) são ignorados por esta consulta.
 *
 * @throws CourseNotFoundException se o curso não existir ou estiver logicamente excluído.
 */
@Component
public class GetCourseByIdUseCase {

    private final CourseRepository courseRepository;

    public GetCourseByIdUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public CourseEntity execute (UUID courseId){
        Objects.requireNonNull(courseId, "courseId é obrigatório");

        return courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(CourseNotFoundException::new);

    }
}
