package com.portal.conecta.hub.module.me.application.use_case;

import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.me.infraestrutura.projection.UserCourseClassProjection;
import com.portal.conecta.hub.module.me.presentation.dto.MyClassResponse;
import com.portal.conecta.hub.module.me.presentation.dto.MyCourseResponse;
import com.portal.conecta.hub.module.me.presentation.dto.MyListCourseResponse;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Component
public class GetMyCoursesUseCase {

    private final ClassMembershipRepository classMembershipRepository;
    private final RequestContextProvider requestContext;

    public GetMyCoursesUseCase(ClassMembershipRepository classMembershipRepository, RequestContextProvider requestContext) {
        this.classMembershipRepository = classMembershipRepository;
        this.requestContext = requestContext;
    }

    public MyListCourseResponse execute(){
        UUID userId = requestContext
                .getRequestContext()
                .userId();

        List<UserCourseClassProjection> rows =
                classMembershipRepository.findCoursesByUserId(userId);

        LinkedHashMap<UUID, MyCourseResponse> coursesMap =
                new LinkedHashMap<>();

        for (UserCourseClassProjection row : rows){
            MyCourseResponse course =
                    coursesMap.computeIfAbsent(
                            row.getCourseId(),
                            id -> new MyCourseResponse(
                                    row.getCourseId(),
                                    row.getCourseName(),
                                    row.getCourseCode(),
                                    new ArrayList<>()
                            )
                    );

            course.classes().add(
                    new MyClassResponse(
                            row.getClassId(),
                            row.getClassName(),
                            row.getClassNumber(),
                            row.getClassShift(),
                            row.getRole()
                    )
            );
        }

        return new MyListCourseResponse(
                new ArrayList<>(coursesMap.values())
        );
    }


}
