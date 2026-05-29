package com.portal.conecta.hub.module.me.application.use_case;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.me.infraestrutura.projection.UserCourseClassProjection;
import com.portal.conecta.hub.module.me.presentation.dto.MyListCourseResponse;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyCoursesUseCaseTest {

    @Mock
    private ClassMembershipRepository classMembershipRepository;

    @Mock
    private RequestContextProvider requestContextProvider;

    @Mock
    private RequestContext requestContext;

    @InjectMocks
    private GetMyCoursesUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        when(requestContextProvider.getRequestContext())
                .thenReturn(requestContext);

        when(requestContext.userId())
                .thenReturn(userId);
    }

    @Test
    void shouldReturnGroupedCoursesSuccessfully() {

        UUID courseId = UUID.randomUUID();

        UUID classId1 = UUID.randomUUID();
        UUID classId2 = UUID.randomUUID();

        UserCourseClassProjection row1 =
                createProjection(
                        courseId,
                        "Desenvolvimento de Sistemas",
                        "DS",
                        classId1,
                        "DS1",
                        1,
                        Shift.FULL_AM_PM,
                        ClassRole.STUDENT
                );

        UserCourseClassProjection row2 =
                createProjection(
                        courseId,
                        "Desenvolvimento de Sistemas",
                        "DS",
                        classId2,
                        "DS2",
                        2,
                        Shift.FULL_PM_NT,
                        ClassRole.REPRESENTATIVE
                );

        when(classMembershipRepository.findCoursesByUserId(userId))
                .thenReturn(List.of(row1, row2));

        MyListCourseResponse response = useCase.execute();

        assertNotNull(response);
        assertEquals(1, response.courses().size());

        var course = response.courses().getFirst();

        assertEquals(courseId, course.id());
        assertEquals("Desenvolvimento de Sistemas", course.name());
        assertEquals("DS", course.code());

        assertEquals(2, course.classes().size());

        var firstClass = course.classes().get(0);

        assertEquals(classId1, firstClass.id());
        assertEquals("DS1", firstClass.name());
        assertEquals(1, firstClass.number());
        assertEquals(Shift.FULL_AM_PM, firstClass.shift());
        assertEquals(ClassRole.STUDENT, firstClass.classRole());

        verify(classMembershipRepository)
                .findCoursesByUserId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoCourses() {

        when(classMembershipRepository.findCoursesByUserId(userId))
                .thenReturn(List.of());

        MyListCourseResponse response = useCase.execute();

        assertNotNull(response);
        assertTrue(response.courses().isEmpty());

        verify(classMembershipRepository)
                .findCoursesByUserId(userId);
    }

    private UserCourseClassProjection createProjection(
            UUID courseId,
            String courseName,
            String courseCode,
            UUID classId,
            String className,
            Integer classNumber,
            Shift shift,
            ClassRole role
    ) {

        return new UserCourseClassProjection() {

            @Override
            public UUID getCourseId() {
                return courseId;
            }

            @Override
            public String getCourseName() {
                return courseName;
            }

            @Override
            public String getCourseCode() {
                return courseCode;
            }

            @Override
            public UUID getClassId() {
                return classId;
            }

            @Override
            public String getClassName() {
                return className;
            }

            @Override
            public Integer getClassNumber() {
                return classNumber;
            }

            @Override
            public Shift getClassShift() {
                return shift;
            }

            @Override
            public ClassRole getRole() {
                return role;
            }
        };
    }
}