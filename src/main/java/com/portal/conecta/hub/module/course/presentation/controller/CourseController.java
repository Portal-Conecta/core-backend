package com.portal.conecta.hub.module.course.presentation.controller;

import com.portal.conecta.hub.module.course.application.command.CreateCourseCommand;
import com.portal.conecta.hub.module.course.application.command.UpdateCourseCommand;
import com.portal.conecta.hub.module.course.application.use_case.CreateCourseUseCase;
import com.portal.conecta.hub.module.course.application.use_case.UpdateCourseUseCase;
import com.portal.conecta.hub.module.course.application.use_case.GetAllCoursesUseCase;
import com.portal.conecta.hub.module.course.application.use_case.GetCourseByIdUseCase;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.presentation.dto.CourseResponse;
import com.portal.conecta.hub.module.course.presentation.dto.CreateCourseRequest;
import com.portal.conecta.hub.module.course.presentation.dto.CreateCourseResponse;
import com.portal.conecta.hub.module.course.presentation.dto.UpdateCourseRequest;
import com.portal.conecta.hub.module.course.presentation.dto.UpdateCourseResponse;
import com.portal.conecta.hub.module.course.presentation.dto.ListCoursesResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CreateCourseUseCase createCourseUseCase;
    private final UpdateCourseUseCase updateCourseUseCase;
    private final GetAllCoursesUseCase getAllCoursesUseCase;
    private final GetCourseByIdUseCase getCourseByIdUseCase;

    public CourseController(CreateCourseUseCase createCourseUseCase,
                            UpdateCourseUseCase updateCourseUseCase,
                            GetAllCoursesUseCase getAllCoursesUseCase,
                            GetCourseByIdUseCase getCourseByIdUseCase) {
        this.createCourseUseCase = createCourseUseCase;
        this.updateCourseUseCase = updateCourseUseCase;
        this.getAllCoursesUseCase = getAllCoursesUseCase;
        this.getCourseByIdUseCase = getCourseByIdUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateCourseResponse> create(@RequestBody CreateCourseRequest createCourseRequest) {
        CourseEntity createdCourse = createCourseUseCase.execute(new CreateCourseCommand(
                createCourseRequest.name(),
                createCourseRequest.code()
        ));

        return ResponseEntity.created(URI.create("/courses/" + createdCourse.getId()))
                .body(CreateCourseResponse.from(createdCourse));
    }

    @GetMapping
    public ResponseEntity<ListCoursesResponse> findAll (){
        return ResponseEntity.ok(
                ListCoursesResponse.from(getAllCoursesUseCase.execute())
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> findById (@PathVariable UUID courseId){
        return ResponseEntity.ok(CourseResponse.from(getCourseByIdUseCase.execute(courseId)));
    }

    @PatchMapping("/{courseId}")
    public ResponseEntity<UpdateCourseResponse> update(@PathVariable UUID courseId, @RequestBody UpdateCourseRequest updateCourseRequest) {
        CourseEntity updatedCourse = updateCourseUseCase.execute(new UpdateCourseCommand(
                courseId,
                updateCourseRequest.name(),
                updateCourseRequest.code()
        ));

        return ResponseEntity.ok(UpdateCourseResponse.from(updatedCourse));
    }
}