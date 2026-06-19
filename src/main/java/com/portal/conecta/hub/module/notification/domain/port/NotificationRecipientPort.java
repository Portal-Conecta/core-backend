package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;

import java.util.List;
import java.util.UUID;

public interface NotificationRecipientPort {

    List<UUID> resolve (
            ProcessNotificationRequestCommand.CommandScope scope,
            List<ProcessNotificationRequestCommand.CommandFilter> filters
    );
}
