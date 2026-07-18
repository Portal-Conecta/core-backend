package com.portal.conecta.hub.module.user.presentation.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.presentation.dto.request.ListUsersRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListUsersRequestTest {

    @Test
    void toQueryUsesDefaultPaginationWhenParametersAreMissing() {
        GetAllUserQuery query = new ListUsersRequest(null, null, TypeUser.STUDENT, null, null).toQuery();

        assertEquals(0, query.page());
        assertEquals(20, query.size());
        assertEquals(TypeUser.STUDENT, query.typeUser());
        assertNull(query.name());
        assertEquals(List.of(AccountStatus.ACTIVE), query.accountStatuses());
    }

    @Test
    void toQueryKeepsProvidedPaginationAndFilter() {
        GetAllUserQuery query = new ListUsersRequest(
                2,
                50,
                TypeUser.TEACHER,
                "Ana",
                List.of("ACTIVE", "DISABLED")
        ).toQuery();

        assertEquals(2, query.page());
        assertEquals(50, query.size());
        assertEquals(TypeUser.TEACHER, query.typeUser());
        assertEquals("Ana", query.name());
        assertEquals(List.of(AccountStatus.ACTIVE, AccountStatus.DISABLED), query.accountStatuses());
    }
}
