package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;

import java.util.EnumSet;

/**
 * Filtros de destinatários normalizados antes da resolução dos escopos.
 *
 * <p>Conjuntos vazios indicam que o grupo de filtro correspondente não foi informado.
 * Os valores de cada conjunto são combinados por união; os resolvers aplicam os grupos
 * de role e shift conjuntamente ao escopo.</p>
 */
public record NotificationRecipientFilters(
        EnumSet<TypeUser> roleTypes,
        EnumSet<Shift> shifts
) {
    public NotificationRecipientFilters {
        roleTypes = copyRoles(roleTypes);
        shifts = copyShifts(shifts);
    }

    private static EnumSet<TypeUser> copyRoles(EnumSet<TypeUser> source) {
        return source.isEmpty() ? EnumSet.noneOf(TypeUser.class) : EnumSet.copyOf(source);
    }

    private static EnumSet<Shift> copyShifts(EnumSet<Shift> source) {
        return source.isEmpty() ? EnumSet.noneOf(Shift.class) : EnumSet.copyOf(source);
    }
}
