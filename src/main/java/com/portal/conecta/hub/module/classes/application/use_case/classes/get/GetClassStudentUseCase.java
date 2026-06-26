package com.portal.conecta.hub.module.classes.application.use_case.classes.get;

import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Retorna os membros com papel de aprendiz de uma turma ativa.
 *
 * <p>Inclui tanto estudantes quanto representantes, pois ambos ocupam
 * posição no mapa de sala. Docentes não são retornados por esta consulta.</p>
 */
@Component
public class GetClassStudentUseCase {

    private final ClassRepository classRepository;
    private final ClassMembershipRepository membershipRepository;

    public GetClassStudentUseCase(ClassRepository classRepository, ClassMembershipRepository membershipRepository) {
        this.classRepository = classRepository;
        this.membershipRepository = membershipRepository;
    }

    /**
     * Executa a consulta de aprendizes da turma.
     *
     * @param classId identificador da turma.
     * @return lista de vínculos com papel {@code STUDENT} ou {@code REPRESENTATIVE};
     *         pode ser vazia se não houver aprendizes vinculados.
     * @throws ClassEntityNotFoundException se a turma não existir ou estiver removida logicamente.
     */
    @Transactional(readOnly = true)
    public List<ClassMembershipEntity> execute (UUID classId){
        Objects.requireNonNull(classId, "O identificador da turma é obrigatório.");

        classRepository.findByIdAndDeletedAtIsNull(classId)
                .orElseThrow(ClassEntityNotFoundException::new);

        return membershipRepository.findActiveStudentsByClassId(
                classId,
                EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE)
        );
    }

}
