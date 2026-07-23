package com.portal.conecta.hub.module.classes.application.use_case.classes.get;

import com.portal.conecta.hub.module.classes.application.query.GetClassMembersQuery;
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

/**
 * Retorna membros não removidos de uma turma, com filtro opcional por papel do vinculo.
 */
@Component
public class GetClassMemberUseCase {

    private final ClassRepository classRepository;
    private final ClassMembershipRepository membershipRepository;

    public GetClassMemberUseCase(ClassRepository classRepository, ClassMembershipRepository membershipRepository) {
        this.classRepository = classRepository;
        this.membershipRepository = membershipRepository;
    }

    /**
     * Executa a consulta de membros não removidos da turma.
     * @param query contendo o identificador da turma e, opcionalmente, o papel do vínculo.
     * @return lista de membros não removidos da turma, filtrados pelo papel se fornecido.
     * @throws ClassEntityNotFoundException se a turma não for encontrada ou estiver desativ
     */
    @Transactional(readOnly = true)
    public List<ClassMembershipEntity> execute (GetClassMembersQuery query) {
        Objects.requireNonNull(query, "A consulta de membros da turma é obrigatória.");
        Objects.requireNonNull(query.classId(), "O identificador da turma é obrigatório.");

        classRepository.findByIdAndDeletedAtIsNull(query.classId())
                .orElseThrow(ClassEntityNotFoundException::new);

        EnumSet<ClassRole> roles = query.role() == null
                ? EnumSet.allOf(ClassRole.class)
                : EnumSet.of(query.role());

        return membershipRepository.findNonRemovedMembersByClassIdAndRoles(
                query.classId(),
                roles
        );
    }

}
