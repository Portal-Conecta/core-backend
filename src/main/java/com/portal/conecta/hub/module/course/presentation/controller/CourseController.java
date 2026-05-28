package com.portal.conecta.hub.module.course.presentation.controller;

import com.portal.conecta.hub.module.course.application.command.CreateCourseCommand;
import com.portal.conecta.hub.module.course.application.use_case.CreateCourseUseCase;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.presentation.dto.CreateCourseRequest;
import com.portal.conecta.hub.module.course.presentation.dto.CreateCourseResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CreateCourseUseCase createCourseUseCase;

    public CourseController(CreateCourseUseCase createCourseUseCase) {
        this.createCourseUseCase = createCourseUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateCourseResponse> create(@Valid @RequestBody CreateCourseRequest createCourseRequest) {
        CourseEntity createdCourse = createCourseUseCase.execute(new CreateCourseCommand(
                createCourseRequest.name(),
                createCourseRequest.code()
        ));

        return ResponseEntity.created(URI.create("/courses/" + createdCourse.getId()))
                .body(CreateCourseResponse.from(createdCourse));
    }
}