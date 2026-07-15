package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.query.GetClassMembersQuery;
import com.portal.conecta.hub.module.classes.application.use_case.classes.get.GetClassMemberUseCase;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetClassMemberUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private ClassMembershipRepository membershipRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GetClassMemberUseCase useCase;

    private UUID classId;
    private ClassEntity classEntity;
    private UserEntity student;
    private UserEntity teacher;
    private UserEntity representative;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        classEntity = new ClassEntity(Shift.FULL_AM_PM, 1, "MIDS1", course);

        lenient().when(passwordEncoder.encode(any())).thenReturn("hashed-password");

        student = createUser("Aluno Teste", "aluno@estudante.sesisenai.org.br", TypeUser.STUDENT);
        teacher = createUser("Professor Teste", "professor@edu.sc.senai.br", TypeUser.TEACHER);
        representative = createUser("Representante Teste", "rep@estudante.sesisenai.org.br", TypeUser.REPRESENTATIVE);
    }

    @Test
    @DisplayName("deve retornar todos os membros ativos quando role não é informado")
    void shouldReturnAllMembersWhenRoleIsNotProvided() {
        List<ClassMembershipEntity> memberships = List.of(
                new ClassMembershipEntity(student, classEntity, ClassRole.STUDENT),
                new ClassMembershipEntity(teacher, classEntity, ClassRole.TEACHER),
                new ClassMembershipEntity(representative, classEntity, ClassRole.REPRESENTATIVE)
        );

        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.of(classEntity));
        when(membershipRepository.findActiveMembersByClassIdAndRoles(classId, EnumSet.allOf(ClassRole.class)))
                .thenReturn(memberships);

        List<ClassMembershipEntity> result = useCase.execute(GetClassMembersQuery.from(classId, null));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(ClassMembershipEntity::getClassRole)
                .containsExactlyInAnyOrder(ClassRole.STUDENT, ClassRole.TEACHER, ClassRole.REPRESENTATIVE);

        verify(classRepository).findByIdAndDeletedAtIsNull(classId);
        verify(membershipRepository).findActiveMembersByClassIdAndRoles(classId, EnumSet.allOf(ClassRole.class));
    }

    @Test
    @DisplayName("deve filtrar membros por STUDENT")
    void shouldFilterMembersByStudentRole() {
        shouldFilterMembersByRole(ClassRole.STUDENT, student);
    }

    @Test
    @DisplayName("deve filtrar membros por TEACHER")
    void shouldFilterMembersByTeacherRole() {
        shouldFilterMembersByRole(ClassRole.TEACHER, teacher);
    }

    @Test
    @DisplayName("deve filtrar membros por REPRESENTATIVE")
    void shouldFilterMembersByRepresentativeRole() {
        shouldFilterMembersByRole(ClassRole.REPRESENTATIVE, representative);
    }

    @Test
    @DisplayName("deve retornar lista vazia quando não há membros no papel solicitado")
    void shouldReturnEmptyListWhenNoMembersMatchRole() {
        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.of(classEntity));
        when(membershipRepository.findActiveMembersByClassIdAndRoles(classId, EnumSet.of(ClassRole.TEACHER)))
                .thenReturn(List.of());

        List<ClassMembershipEntity> result = useCase.execute(GetClassMembersQuery.from(classId, ClassRole.TEACHER));

        assertThat(result).isEmpty();
        verify(membershipRepository).findActiveMembersByClassIdAndRoles(classId, EnumSet.of(ClassRole.TEACHER));
    }

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma não existe")
    void shouldThrowWhenClassNotFound() {
        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(GetClassMembersQuery.from(classId, ClassRole.STUDENT)))
                .isInstanceOf(ClassEntityNotFoundException.class);

        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando query é nula")
    void shouldThrowWhenQueryIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(classRepository, membershipRepository);
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando classId é nulo")
    void shouldThrowWhenClassIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(GetClassMembersQuery.from(null, ClassRole.STUDENT)))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(classRepository, membershipRepository);
    }

    private void shouldFilterMembersByRole(ClassRole role, UserEntity user) {
        ClassMembershipEntity membership = new ClassMembershipEntity(user, classEntity, role);

        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.of(classEntity));
        when(membershipRepository.findActiveMembersByClassIdAndRoles(classId, EnumSet.of(role)))
                .thenReturn(List.of(membership));

        List<ClassMembershipEntity> result = useCase.execute(GetClassMembersQuery.from(classId, role));

        assertThat(result).singleElement()
                .extracting(ClassMembershipEntity::getClassRole)
                .isEqualTo(role);
        verify(membershipRepository).findActiveMembersByClassIdAndRoles(classId, EnumSet.of(role));
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
