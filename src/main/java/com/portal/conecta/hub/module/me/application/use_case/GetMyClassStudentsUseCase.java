package com.portal.conecta.hub.module.me.application.use_case;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.presentation.dto.response.ClassMemberResponse;
import com.portal.conecta.hub.shared.context.ContextClass;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Component
public class GetMyClassStudentsUseCase {

    private static final EnumSet<ClassRole> STUDENT_ROLES = EnumSet.of(
            ClassRole.STUDENT,
            ClassRole.REPRESENTATIVE
    );

    private final ClassMembershipRepository classMembershipRepository;
    private final RequestContextProvider requestContextProvider;

    public GetMyClassStudentsUseCase(
            ClassMembershipRepository classMembershipRepository,
            RequestContextProvider requestContextProvider
    ) {
        this.classMembershipRepository = classMembershipRepository;
        this.requestContextProvider = requestContextProvider;
    }

    @Transactional(readOnly = true)
    public List<ClassMemberResponse> execute() {
        RequestContext context = requestContextProvider.getRequestContext();

        List<UUID> classIds = classMembershipRepository.findActiveByUserId(context.userId())
                .stream()
                .map(membership -> membership.getClassEntity().getId())
                .distinct()
                .toList();

        if (classIds.isEmpty()) {
            classIds = context.classes()
                    .stream()
                    .map(ContextClass::classId)
                    .distinct()
                    .toList();
        }

        if (classIds.isEmpty()) {
            return List.of();
        }

        return classMembershipRepository.findNonRemovedMembersByClassIdsAndRoles(classIds, STUDENT_ROLES)
                .stream()
                .map(ClassMemberResponse::from)
                .toList();
    }
}
