package com.portal.conecta.hub.module.user.application.command;

import org.springframework.web.multipart.MultipartFile;

public record ImportUsersCommand(
        MultipartFile file,
        ExistingEmailHandling existingEmailHandling,
        boolean dryRun
) {
    public enum ExistingEmailHandling {
        REJECT,
        SKIP
    }
}
