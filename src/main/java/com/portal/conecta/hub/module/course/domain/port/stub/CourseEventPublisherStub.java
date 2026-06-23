package com.portal.conecta.hub.module.course.domain.port.stub;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("test")
public class CourseEventPublisherStub implements CourseEventPublisher {

    private final List<String> publishedEvents = new ArrayList<>();

    @Override
    public void publishCreated(CourseEntity course) {
        publishedEvents.add("course.created:" + course.getId());
    }

    @Override
    public void publishUpdated(CourseEntity course) {
        publishedEvents.add("course.updated:" + course.getId());
    }

    @Override
    public void publishDeleted(CourseEntity course) {
        publishedEvents.add("course.deleted:" + course.getId());
    }

    public List<String> getPublishedEvents() {
        return List.copyOf(publishedEvents);
    }

    public void reset() {
        publishedEvents.clear();
    }
}