package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ClassMemberResponse(
        @Schema(description = "Identificador do usuário vinculado à turma.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Nome do usuário vinculado à turma.", example = "Maria Silva")
        String name,

        @Schema(description = "Papel acadêmico do usuário dentro da turma.", example = "STUDENT")
        ClassRole classRole,

        @Schema(description = "Indica se a conta já foi ativada. Usuários pendentes aparecem com active=false.", example = "false")
        boolean active,

        @Schema(description = "Status calculado da conta a partir de active/deletedAt.", example = "PENDING_ACTIVATION")
        AccountStatus accountStatus
) {
    public static ClassMemberResponse from(ClassMembershipEntity membership){
        return new ClassMemberResponse(
                membership.getUser().getId(),
                membership.getUser().getName(),
                membership.getClassRole(),
                membership.getUser().isActive(),
                membership.getUser().getAccountStatus()
        );
    }
}
