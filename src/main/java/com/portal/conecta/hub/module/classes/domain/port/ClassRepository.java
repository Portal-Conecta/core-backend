package com.portal.conecta.hub.module.classes.domain.port;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de acesso a dados para turmas.
 *
 * <p>Métodos com {@code DeletedAtIsNull} no nome excluem turmas removidas logicamente.
 * Suporta consultas dinâmicas via {@link JpaSpecificationExecutor} para listagens filtradas.</p>
 */
public interface ClassRepository extends JpaRepository<ClassEntity, UUID>, JpaSpecificationExecutor<ClassEntity> {

    /**
     * Busca a turma pelo ID aplicando bloqueio pessimista de escrita.
     * Utilizado na promoção de representante para evitar condição de corrida
     * na contagem de vagas disponíveis.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ClassEntity c WHERE c.id = :id")
    Optional<ClassEntity> findByIdForUpdate(@Param("id") UUID id);

    /**
     * Busca uma turma ativa pelo ID, excluindo turmas removidas logicamente.
     */
    Optional<ClassEntity> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Retorna turmas ativas pelos IDs informados, excluindo removidas logicamente.
     */
    @Query(value = """
    SELECT c FROM ClassEntity c WHERE c.id IN :ids AND c.deletedAt IS NULL
        """)
    List<ClassEntity> findAllByIdInAndDeletedAtIsNull(@Param("ids") List<UUID> ids);

    /**
     * Retorna turmas pelos IDs informados sem filtro de estado.
     * Inclui turmas removidas logicamente e desativadas.
     */
    @Query("""
    SELECT c FROM ClassEntity c WHERE c.id IN :ids
    """)
    List<ClassEntity> findAllByIdIn(@Param("ids") List<UUID> ids);

    /**
     * Retorna turmas pelos IDs informados, excluindo removidas logicamente.
     *
     * <p>Equivalente a {@link #findAllByIdInAndDeletedAtIsNull}, mas nomeado
     * para uso em contextos onde a semântica de "não deletado" deve ficar
     * explícita na chamada.</p>
     */
    @Query("""
    SELECT c FROM ClassEntity c
    WHERE c.id IN :ids
    AND c.deletedAt IS NULL
    """)
    List<ClassEntity> findAllByIdsNotDeleted(@Param("ids") List<UUID> ids);

    /**
     * Verifica se já existe turma ativa com o número informado dentro do curso.
     * Utilizado para garantir unicidade de número por curso antes da criação.
     */
    boolean existsByNumberAndCourseIdAndDeletedAtIsNull(Integer number, UUID courseId);

    /**
     * Atualiza em lote o nome das turmas de um curso quando o código do curso muda.
     *
     * <p>O nome da turma é derivado de {@code course.code + class.number}; esta operação evita
     * carregar todas as turmas do curso apenas para recomputar esse campo materializado.</p>
     */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE classes
            SET name = CONCAT(:courseCode, number),
                updated_at = CURRENT_TIMESTAMP,
                updated_by = :updatedBy
            WHERE course_id = :courseId
            """, nativeQuery = true)
    int updateNamesByCourseId(
            @Param("courseId") UUID courseId,
            @Param("courseCode") String courseCode,
            @Param("updatedBy") UUID updatedBy
    );

    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE classes SET id = :id WHERE name = :name", nativeQuery = true)
    int updateIdByName(@Param("id") UUID id, @Param("name") String name);

}
