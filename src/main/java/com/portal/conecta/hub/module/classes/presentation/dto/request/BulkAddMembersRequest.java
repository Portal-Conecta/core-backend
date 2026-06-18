package com.portal.conecta.hub.module.classes.presentation.dto.request;

import com.portal.conecta.hub.module.classes.application.command.BulkAddMembersCommand;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkAddMembersRequest(

        @NotEmpty(message = "A lista de membros não pode estar vazia.")
        List<MemberItem> members
) {

    public record MemberItem(
            @NotNull(message = "userId é obrigatório.")
            UUID userId,

            @NotNull(message = "classRole é obrigatório.")
            ClassRole classRole
    ) {

    }

    public BulkAddMembersCommand toCommand (UUID classId){
        return new BulkAddMembersCommand(
            classId,
                members.stream()
                        .map(m-> new BulkAddMembersCommand.Item(m.userId(), m.classRole))
                        .toList()
        );
    }

}
