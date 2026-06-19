package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;

import java.util.List;

public interface NotificationRecipientPort {

   void dispatch(
           NotificationEntity notification,
           List<ProcessNotificationRequestCommand.CommandScope> scopes,
           List<ProcessNotificationRequestCommand.CommandFilter> filters
   );
}
