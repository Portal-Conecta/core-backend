package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand.CommandFilter;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

/**
 * Normaliza filtros do payload para os critérios usados pelos resolvers de escopo.
 */
@Component
public class NotificationRecipientFilterResolver {

    /**
     * Converte os filtros do payload em conjuntos de valores permitidos.
     *
     * <p>Valores do mesmo grupo são unidos. STUDENT representa o aluno em sentido
     * acadêmico e, por isso, também inclui REPRESENTATIVE.</p>
     */
    public NotificationRecipientFilters resolve(List<CommandFilter> filters) {
        EnumSet<TypeUser> roleTypes = EnumSet.noneOf(TypeUser.class);
        EnumSet<Shift> shifts = EnumSet.noneOf(Shift.class);

        for (CommandFilter filter : filters) {
            switch (filter.type()) {
                case ROLE -> addRole(filter.value(), roleTypes);
                case SHIFT -> shifts.add(parseShift(filter.value()));
            }
        }

        return new NotificationRecipientFilters(roleTypes, shifts);
    }

    private void addRole(String value, EnumSet<TypeUser> roleTypes) {
        TypeUser type = parseRole(value);
        roleTypes.add(type);
        if (type == TypeUser.STUDENT) {
            roleTypes.add(TypeUser.REPRESENTATIVE);
        }
    }

    private TypeUser parseRole(String value) {
        try {
            return TypeUser.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidNotificationPayloadException("Invalid ROLE filter value: " + value);
        }
    }

    private Shift parseShift(String value) {
        try {
            return Shift.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidNotificationPayloadException("Invalid SHIFT filter value: " + value);
        }
    }
}
