package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.application.result.ClassImportResult;
import java.util.List;

public record ClassImportResponse(
        boolean dryRun,
        int created,
        int skipped,
        List<RowResponse> rows
) {
    public static ClassImportResponse from(ClassImportResult result) {
        return new ClassImportResponse(result.dryRun(), result.created(), result.skipped(), result.rows().stream()
                .map(row -> new RowResponse(row.line(), row.status(), row.message()))
                .toList());
    }

    public record RowResponse(int line, ClassImportResult.Status status, String message) {
    }
}
