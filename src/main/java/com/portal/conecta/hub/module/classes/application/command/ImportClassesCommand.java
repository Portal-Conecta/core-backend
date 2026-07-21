package com.portal.conecta.hub.module.classes.application.command;

import org.springframework.web.multipart.MultipartFile;

public record ImportClassesCommand(
        MultipartFile file,
        ExistingClassHandling existingClassHandling,
        boolean dryRun
) {
    public enum ExistingClassHandling {
        REJECT,
        SKIP
    }
}
