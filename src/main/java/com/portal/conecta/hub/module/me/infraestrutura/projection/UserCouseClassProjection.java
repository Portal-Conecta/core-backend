package com.portal.conecta.hub.module.me.infraestrutura.projection;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;

import java.util.UUID;

public interface UserCouseClassProjection {

    UUID getCourseId();
    String getCourseName();
    String getCourseCode();

    UUID getClassId();
    String getClassName();
    Integer getClassNumber();
    Shift getClassShift();

    ClassRole getRole();
}
