package com.portal.conecta.hub.module.notification.domain.port.stub;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
public class NotificationRecipientPortStub implements NotificationRecipientPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationRecipientPortStub.class);

    @Override
    public void dispatch(NotificationEntity notification,
                         List<ProcessNotificationRequestCommand.CommandScope> scopes,
                         List<ProcessNotificationRequestCommand.CommandFilter> filters) {

        log.info("[STUB] dispatch called for notificationId={}, scopes={}, filters={}",
                notification.getId(), scopes, filters);
    }
}
