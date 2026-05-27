package com.portal.conecta.hub.module.classes.presentation.dto;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(
        @NotNull UUID userId,
        @NotNull ClassRole classRole
) {
}
