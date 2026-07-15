package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record AddMemberResponse(
        @Schema(description = "Identificador do usuário vinculado à turma.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,

        @Schema(description = "Nome do usuário vinculado à turma.", example = "Maria Silva")
        String userName,

        @Schema(description = "Identificador da turma que recebeu o vínculo.", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID classId,

        @Schema(description = "Papel acadêmico atribuído ao usuário na turma.", example = "STUDENT")
        ClassRole classRole,

        @Schema(description = "Indica se a conta já foi ativada. Usuários pendentes aparecem com active=false.", example = "false")
        boolean active,

        @Schema(description = "Status calculado da conta a partir de active/deletedAt.", example = "PENDING_ACTIVATION")
        AccountStatus accountStatus
) {
    public static AddMemberResponse from(ClassMembershipEntity entity) {
        return new AddMemberResponse(
                entity.getUser().getId(),
                entity.getUser().getName(),
                entity.getClassEntity().getId(),
                entity.getClassRole(),
                entity.getUser().isActive(),
                entity.getUser().getAccountStatus()
        );
    }
}
