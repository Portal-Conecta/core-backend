package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;

import java.util.List;

public record BulkAddMemberResponse(
        List<AddMemberResponse> items
) {

    public static BulkAddMemberResponse from(List<ClassMembershipEntity> membership){
        return new BulkAddMemberResponse(
                membership.stream()
                        .map(AddMemberResponse::from)
                        .toList()
        );
    }
}
