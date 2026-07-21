package com.portal.conecta.hub.module.course.domain.port;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída (Outbound Port) para a persistência de Cursos.
 * <p>
 * Os métodos {@code existsBy...} são fundamentais para validar as restrições de unicidade
 * de nome e código antes de inserções ou atualizações. Buscas (find) devem
 * preferencialmente utilizar os métodos que filtram exclusões lógicas (DeletedAtIsNull).
 */
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsByCodeAndIdNot(String code, UUID id);

    Optional<CourseEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<CourseEntity> findByCodeAndDeletedAtIsNull(String code);

    List<CourseEntity> findAllByDeletedAtIsNull();

    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE courses SET id = :id WHERE code = :code", nativeQuery = true)
    int updateIdByCode(@Param("id") UUID id, @Param("code") String code);
}
