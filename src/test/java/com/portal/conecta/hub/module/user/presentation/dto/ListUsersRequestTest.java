package com.portal.conecta.hub.module.user.presentation.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.presentation.dto.request.ListUsersRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListUsersRequestTest {

    @Test
    void toQueryUsesDefaultPaginationWhenParametersAreMissing() {
        GetAllUserQuery query = new ListUsersRequest(null, null, TypeUser.STUDENT, null, null).toQuery();

        assertEquals(0, query.page());
        assertEquals(20, query.size());
        assertEquals(TypeUser.STUDENT, query.typeUser());
    }

    @Test
    void toQueryKeepsProvidedPaginationAndFilter() {
        UUID excludeClassId = UUID.randomUUID();

        GetAllUserQuery query = new ListUsersRequest(2, 50, TypeUser.TEACHER, "ana", excludeClassId).toQuery();

        assertEquals(2, query.page());
        assertEquals(50, query.size());
        assertEquals(TypeUser.TEACHER, query.typeUser());
        assertEquals("ana", query.name());
        assertEquals(excludeClassId, query.excludeClassId());
    }
}
