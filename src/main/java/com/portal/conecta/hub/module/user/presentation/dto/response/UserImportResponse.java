package com.portal.conecta.hub.module.user.presentation.dto.response;

import com.portal.conecta.hub.module.user.application.result.UserImportResult;
import java.util.List;

public record UserImportResponse(
        boolean dryRun,
        int created,
        int skipped,
        List<RowResponse> rows
) {
    public static UserImportResponse from(UserImportResult result) {
        return new UserImportResponse(result.dryRun(), result.created(), result.skipped(), result.rows().stream()
                .map(row -> new RowResponse(row.line(), row.status(), row.message()))
                .toList());
    }

    public record RowResponse(int line, UserImportResult.Status status, String message) {
    }
}
