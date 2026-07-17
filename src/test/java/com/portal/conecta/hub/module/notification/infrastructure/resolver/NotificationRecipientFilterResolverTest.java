package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand.CommandFilter;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.filter.NotificationFilterType;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationRecipientFilterResolverTest {

    private final NotificationRecipientFilterResolver resolver = new NotificationRecipientFilterResolver();

    @Test
    void expandsStudentRoleToStudentAndRepresentative() {
        NotificationRecipientFilters filters = resolver.resolve(List.of(role("STUDENT")));

        assertThat(filters.roleTypes()).isEqualTo(EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE));
        assertThat(filters.shifts()).isEmpty();
    }

    @Test
    void keepsRepresentativeAndTeacherRolesExact() {
        NotificationRecipientFilters filters = resolver.resolve(List.of(role("REPRESENTATIVE"), role("TEACHER")));

        assertThat(filters.roleTypes()).isEqualTo(EnumSet.of(TypeUser.REPRESENTATIVE, TypeUser.TEACHER));
    }

    @Test
    void unionsRolesAndShiftsInSeparateGroups() {
        NotificationRecipientFilters filters = resolver.resolve(List.of(
                role("STUDENT"), role("TEACHER"), shift("FULL_AM_PM"), shift("FULL_PM_NT")
        ));

        assertThat(filters.roleTypes()).isEqualTo(EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE, TypeUser.TEACHER));
        assertThat(filters.shifts()).isEqualTo(EnumSet.of(Shift.FULL_AM_PM, Shift.FULL_PM_NT));
    }

    @Test
    void rejectsInvalidRoleAndShiftValues() {
        assertThatThrownBy(() -> resolver.resolve(List.of(role("INVALID"))))
                .isInstanceOf(InvalidNotificationPayloadException.class)
                .hasMessageContaining("INVALID");
        assertThatThrownBy(() -> resolver.resolve(List.of(shift("INVALID"))))
                .isInstanceOf(InvalidNotificationPayloadException.class)
                .hasMessageContaining("INVALID");
    }

    private CommandFilter role(String value) {
        return new CommandFilter(NotificationFilterType.ROLE, value);
    }

    private CommandFilter shift(String value) {
        return new CommandFilter(NotificationFilterType.SHIFT, value);
    }
}
