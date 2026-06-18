package com.portal.conecta.hub.module.notification.domain.port.stub;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!prod")
public class NotificationRecipientPortStub implements NotificationRecipientPort {
    @Override
    public List<UserEntity> resolve(
            ProcessNotificationRequestCommand.CommandScope scope,
            List<ProcessNotificationRequestCommand.CommandFilter> filters
    ) {
        return List.of();
    }
}
