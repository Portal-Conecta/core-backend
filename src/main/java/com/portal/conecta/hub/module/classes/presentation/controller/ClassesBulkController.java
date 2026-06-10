package com.portal.conecta.hub.module.classes.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.conecta.hub.module.classes.application.usecase.BulkClassesUseCase;
import com.portal.conecta.hub.module.classes.presentation.dto.BulkClassesRequest;
import com.portal.conecta.hub.module.classes.presentation.dto.BulkClassesResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/classes")
public class ClassesBulkController {

    private final BulkClassesUseCase bulkClassesUseCase;

    public ClassesBulkController(BulkClassesUseCase bulkClassesUseCase) {
        this.bulkClassesUseCase = bulkClassesUseCase;
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkClassesResponse> bulk(@Valid @RequestBody BulkClassesRequest request) {
        BulkClassesResponse response = bulkClassesUseCase.execute(request.ids(), request.includeInactiveOrDefault());
        return ResponseEntity.ok(response);
    }
}
