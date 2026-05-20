package com.portal.conecta.hub.module.classes.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.CreateClassCommand;
import com.portal.conecta.hub.module.classes.application.use_case.CreateClassUseCase;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.presentation.dto.CreateClassRequest;
import com.portal.conecta.hub.module.classes.presentation.dto.CreateClassResponse;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/classes")
public class ClassController {

    private final CreateClassUseCase createClassUseCase;

    public ClassController(CreateClassUseCase createClassUseCase) {
        this.createClassUseCase = createClassUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateClassResponse> create (@RequestBody CreateClassRequest request){
        ClassEntity createdClass = createClassUseCase.execute(new CreateClassCommand(
                request.shift(),
                request.number(),
                request.name(),
                request.courseId()
        ));

        return ResponseEntity.created(URI.create("/classes/" + createdClass.getId()))
                .body(CreateClassResponse.from(createdClass));
    }
}
