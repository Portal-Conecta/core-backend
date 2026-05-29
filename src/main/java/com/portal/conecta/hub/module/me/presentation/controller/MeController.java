package com.portal.conecta.hub.module.me.presentation.controller;

import com.portal.conecta.hub.module.me.application.use_case.GetMyCoursesUseCase;
import com.portal.conecta.hub.module.me.presentation.dto.MyListCourseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class MeController {

    private final GetMyCoursesUseCase getMyCoursesUseCase;

    public MeController(GetMyCoursesUseCase getMyCoursesUseCase) {
        this.getMyCoursesUseCase = getMyCoursesUseCase;
    }

    @GetMapping("/courses")
    public ResponseEntity<MyListCourseResponse> getMyCourses(){
        return ResponseEntity.ok(getMyCoursesUseCase.execute());
    }
}
