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

@Component
public class GetClassStudentUseCase {

    private final ClassRepository classRepository;
    private final ClassMembershipRepository membershipRepository;

    public GetClassStudentUseCase(ClassRepository classRepository, ClassMembershipRepository membershipRepository) {
        this.classRepository = classRepository;
        this.membershipRepository = membershipRepository;
    }

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
