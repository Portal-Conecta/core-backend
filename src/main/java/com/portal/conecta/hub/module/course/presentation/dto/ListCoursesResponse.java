package com.portal.conecta.hub.module.course.presentation.dto;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;

import java.util.List;

public record ListCoursesResponse(List<CourseResponse> courses) {
    public static ListCoursesResponse from (List<CourseEntity> courses){
        return new ListCoursesResponse(
                courses.stream()
                        .map(CourseResponse::from)
                        .toList()
        );
    }
}
