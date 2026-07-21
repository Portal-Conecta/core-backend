package com.portal.conecta.hub.module.user.application.result;

import java.util.List;

public record UserImportResult(
        boolean dryRun,
        int created,
        int skipped,
        List<RowResult> rows
) {
    public record RowResult(int line, Status status, String message) {
    }

    public enum Status {
        CREATED,
        SKIPPED,
        ERROR
    }
}
