package com.portal.conecta.hub.shared.context;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;

import java.util.List;
import java.util.UUID;

/**
 * Context of the request, containing information about the user and their permissions.
 *
 * @param userId   The ID of the user making the request.
 * @param userType The type of the user making the request (e.g., STUDENT, TEACHER, etc.).
 * @param classes   A list of classes the user is associated with, along with their role in each class.
 */
public record RequestContext (
        UUID userId,
        TypeUser userType,
        List<ContextClass> classes
) {
}
