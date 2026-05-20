package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClassPermissionValidator {

    private static final List<TypeUser> PERMISSION_TYPES = List.of(
            TypeUser.ADMIN,
            TypeUser.WEG,
            TypeUser.SENAI
    );

    public boolean canCreate (TypeUser type){
        return PERMISSION_TYPES.contains(type);
    }
}
