package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class GetCourseByIdUseCase {

    private final CourseRepository courseRepository;

    public GetCourseByIdUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public CourseEntity execute (UUID courseId){
        Objects.requireNonNull(courseId, "courseId is required");

        return courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(()-> new CourseNotFoundException("Course not found: " + courseId));

    }
}
