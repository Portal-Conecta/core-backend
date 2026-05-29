package com.portal.conecta.hub.module.classes.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.*;
import com.portal.conecta.hub.module.classes.application.use_case.*;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.presentation.dto.*;
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
    private final PromoteToRepresentativeUseCase promoteToRepresentativeUseCase;
    private final DemoteFromRepresentativeUseCase demoteFromRepresentativeUseCase;
    private final DeleteClassMembershipUseCase deleteClassMembershipUseCase;

    public ClassController(CreateClassUseCase createClassUseCase, DeleteClassUseCase deleteClassUseCase, AddClassMemberUseCase addClassMemberUseCase, PromoteToRepresentativeUseCase promoteToRepresentativeUseCase, DemoteFromRepresentativeUseCase demoteFromRepresentativeUseCase, DeleteClassMembershipUseCase deleteClassMembershipUseCase) {
        this.createClassUseCase = createClassUseCase;
        this.deleteClassUseCase = deleteClassUseCase;
        this.addClassMemberUseCase = addClassMemberUseCase;
        this.promoteToRepresentativeUseCase = promoteToRepresentativeUseCase;
        this.demoteFromRepresentativeUseCase = demoteFromRepresentativeUseCase;
        this.deleteClassMembershipUseCase = deleteClassMembershipUseCase;
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

    @PatchMapping("/{classId}/members/{userId}/representative")
    public ResponseEntity<PromoteMemberResponse> promoteToRepresentative(
            @PathVariable UUID classId,
            @PathVariable UUID userId
    ){
        PromoteMemberCommand command = new PromoteMemberCommand(classId, userId);
        ClassMembershipEntity membership = promoteToRepresentativeUseCase.execute(command);
        return ResponseEntity.ok(PromoteMemberResponse.from(membership));
    }

    @DeleteMapping("/{classId}/members/{userId}/representative")
    public ResponseEntity<DemoteMemberResponse> demoteFromRepresentative(
            @PathVariable UUID classId,
            @PathVariable UUID userId) {

        DemoteMemberCommand command = new DemoteMemberCommand(classId, userId);
        ClassMembershipEntity membership = demoteFromRepresentativeUseCase.execute(command);

        return ResponseEntity.ok(DemoteMemberResponse.from(membership));
    }

    @DeleteMapping("/{classId}/members/{userId}")
    public ResponseEntity<Void> deleteMembership(
            @PathVariable UUID classId,
            @PathVariable UUID userId) {

        DeleteMembershipCommand command = new DeleteMembershipCommand(classId, userId);
        deleteClassMembershipUseCase.execute(command);

        return ResponseEntity.noContent().build(); // Retorna 204
    }

}
