package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class GetClassByIdUseCase {

    private final ClassRepository classRepository;

    public GetClassByIdUseCase(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public ClassEntity execute(UUID classId){
        Objects.requireNonNull(classId, "classId is required");
        return classRepository.findByIdAndDeletedAtIsNull(classId)
                .orElseThrow(()-> new ClassEntityNotFoundException("Class not found " +classId));
    }
}
