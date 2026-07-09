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
 * </p>
 */
@Component
public class GetCourseByIdUseCase {

    private final CourseRepository courseRepository;

    public GetCourseByIdUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Busca os detalhes completos de um curso utilizando seu ID.
     * <p>
     * Aplica uma validação para garantir que o identificador não seja nulo antes de acionar o banco de dados.
     * </p>
     *
     * @param courseId Identificador único (UUID) do curso a ser recuperado.
     * @return CourseEntity A entidade do curso correspondente ao ID informado.
     * @throws NullPointerException Se o parâmetro {@code courseId} for nulo.
     * @throws CourseNotFoundException Se o curso não for encontrado na base ou se estiver marcado como deletado.
     */
    public CourseEntity execute (UUID courseId){
        Objects.requireNonNull(courseId, "courseId é obrigatório");

        return courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(CourseNotFoundException::new);

    }
}
