package com.portal.conecta.hub.module.notification.domain.port.stub;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
@Slf4j
public class NotificationRecipientPortStub implements NotificationRecipientPort {

    @Override
    public void dispatch(NotificationEntity notification,
                         List<ProcessNotificationRequestCommand.CommandScope> scopes,
                         List<ProcessNotificationRequestCommand.CommandFilter> filters) {

        log.debug("Stub de destinatários de notificação acionado. notificationId={}, scopeCount={}, filterCount={}",
                notification.getId(), scopes.size(), filters.size());
    }
}