package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;

import java.util.List;

/**
 * Porta de domínio responsável por materializar destinatários de uma notificação.
 *
 * <p>A notificação global já deve existir antes da chamada. A implementação cruza escopos
 * e filtros para criar os vínculos em {@code user_notifications}, evitando duplicidade
 * para o mesmo par notificação/usuário.</p>
 */
public interface NotificationRecipientPort {

   /**
    * Distribui uma notificação para os usuários resolvidos a partir dos escopos e filtros.
    *
    * @param notification notificação global persistida.
    * @param scopes escopos de distribuição informados pela mensagem.
    * @param filters filtros opcionais aplicados aos destinatários.
    */
   void dispatch(
           NotificationEntity notification,
           List<ProcessNotificationRequestCommand.CommandScope> scopes,
           List<ProcessNotificationRequestCommand.CommandFilter> filters
   );
}
