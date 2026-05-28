package com.portal.conecta.hub.module.classes.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.AddMemberCommand;
import com.portal.conecta.hub.module.classes.application.command.CreateClassCommand;
import com.portal.conecta.hub.module.classes.application.use_case.AddClassMemberUseCase;
import com.portal.conecta.hub.module.classes.application.use_case.CreateClassUseCase;
import com.portal.conecta.hub.module.classes.application.use_case.DeleteClassUseCase;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.presentation.dto.AddMemberRequest;
import com.portal.conecta.hub.module.classes.presentation.dto.AddMemberResponse;
import com.portal.conecta.hub.module.classes.presentation.dto.CreateClassRequest;
import com.portal.conecta.hub.module.classes.presentation.dto.CreateClassResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/classes")
public class ClassController {

    private final CreateClassUseCase createClassUseCase;
    private final DeleteClassUseCase deleteClassUseCase;
    private final AddClassMemberUseCase addClassMemberUseCase;

    public ClassController(CreateClassUseCase createClassUseCase, DeleteClassUseCase deleteClassUseCase, AddClassMemberUseCase addClassMemberUseCase) {
        this.createClassUseCase = createClassUseCase;
        this.deleteClassUseCase = deleteClassUseCase;
        this.addClassMemberUseCase = addClassMemberUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateClassResponse> create (@Valid @RequestBody CreateClassRequest request){
        ClassEntity createdClass = createClassUseCase.execute(new CreateClassCommand(
                request.shift(),
                request.courseId()
        ));

        return ResponseEntity.created(URI.create("/classes/" + createdClass.getId()))
                .body(CreateClassResponse.from(createdClass));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete (@PathVariable UUID id){
        deleteClassUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{classId}/members")
    public ResponseEntity<AddMemberResponse> addMember(
            @PathVariable UUID classId,
            @Valid @RequestBody AddMemberRequest request
    ){
        AddMemberCommand command = new AddMemberCommand(classId,request.userId(),request.classRole());
        ClassMembershipEntity membership = addClassMemberUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AddMemberResponse.from(membership));
    }
}
