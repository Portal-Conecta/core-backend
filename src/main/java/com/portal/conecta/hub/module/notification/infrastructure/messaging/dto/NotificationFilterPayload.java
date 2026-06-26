package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import com.portal.conecta.hub.module.notification.domain.model.NotificationFilterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Filtro aplicado sobre os destinatários resolvidos a partir dos escopos da mensagem.
 *
 * <p>Atualmente o filtro suportado é {@code ROLE}, usado para limitar a distribuição
 * aos usuários cujo tipo global corresponda ao valor informado.</p>
 *
 * @param type tipo de filtro aceito pelo contrato de notificação.
 * @param value valor usado para aplicar o filtro.
 */
public record NotificationFilterPayload(
        @NotNull(message = "O tipo do filtro (type) é obrigatório.")
        NotificationFilterType type,

        @NotBlank(message = "O valor do filtro (value) é obrigatório.")
        String value
) {}
