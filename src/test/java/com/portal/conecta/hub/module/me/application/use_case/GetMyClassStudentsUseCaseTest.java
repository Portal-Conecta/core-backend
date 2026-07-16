package com.portal.conecta.hub.module.me.application.use_case;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.shared.context.ContextClass;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyClassStudentsUseCaseTest {

    @Mock
    private ClassMembershipRepository classMembershipRepository;

    @Mock
    private RequestContextProvider requestContextProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    private GetMyClassStudentsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMyClassStudentsUseCase(classMembershipRepository, requestContextProvider);
        lenient().when(passwordEncoder.encode("senha123")).thenReturn("hashed-password");
    }

    @Test
    void shouldReturnStudentsAndRepresentativesFromContextClasses() {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID otherClassId = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "DS");
        ClassEntity classEntity = new ClassEntity(Shift.FULL_AM_PM, 1, "DS1", course);

        UserEntity student = createUser("Aluno", "aluno@estudante.sesisenai.org.br", TypeUser.STUDENT);
        UserEntity representative = createUser("Representante", "rep@estudante.sesisenai.org.br", TypeUser.REPRESENTATIVE);

        List<ClassMembershipEntity> memberships = List.of(
                new ClassMembershipEntity(student, classEntity, ClassRole.STUDENT),
                new ClassMembershipEntity(representative, classEntity, ClassRole.REPRESENTATIVE)
        );

        when(requestContextProvider.getRequestContext()).thenReturn(new RequestContext(
                userId,
                TypeUser.TEACHER,
                List.of(
                        new ContextClass(classId, ClassRole.TEACHER),
                        new ContextClass(classId, ClassRole.TEACHER),
                        new ContextClass(otherClassId, ClassRole.TEACHER)
                )
        ));
        when(classMembershipRepository.findNonRemovedMembersByClassIdsAndRoles(
                List.of(classId, otherClassId),
                EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE)
        )).thenReturn(memberships);

        var response = useCase.execute();

        assertThat(response).hasSize(2);
        assertThat(response).extracting("name").containsExactly("Aluno", "Representante");
        assertThat(response).extracting("classRole").containsExactly(ClassRole.STUDENT, ClassRole.REPRESENTATIVE);

        verify(classMembershipRepository).findNonRemovedMembersByClassIdsAndRoles(
                List.of(classId, otherClassId),
                EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE)
        );
    }

    @Test
    void shouldReturnEmptyListWhenContextHasNoClasses() {
        when(requestContextProvider.getRequestContext()).thenReturn(new RequestContext(
                UUID.randomUUID(),
                TypeUser.STUDENT,
                List.of()
        ));

        var response = useCase.execute();

        assertThat(response).isEmpty();
        verifyNoInteractions(classMembershipRepository);
    }

    private UserEntity createUser(String name, String email, TypeUser typeUser) {
        return UserEntity.create(
                name,
                email,
                "senha123",
                typeUser,
                null,
                passwordEncoder
        );
    }
}
