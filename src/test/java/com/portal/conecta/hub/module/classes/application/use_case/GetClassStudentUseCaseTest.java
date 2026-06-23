package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.use_case.classes.get.GetClassStudentUseCase;
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
class GetClassStudentsUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private ClassMembershipRepository membershipRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GetClassStudentUseCase useCase;

    private UUID classId;
    private ClassEntity classEntity;
    private CourseEntity course;
    private UserEntity student;
    private UserEntity representative;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        classEntity = new ClassEntity(Shift.FULL_AM_PM, 1, "MIDS1", course);

        lenient().when(passwordEncoder.encode(any())).thenReturn("hashed-password");

        student = UserEntity.create(
                "Aluno Teste",
                "aluno@estudante.sesisenai.org.br",
                "senha123",
                TypeUser.STUDENT,
                null,
                passwordEncoder
        );

        representative = UserEntity.create(
                "Representante Teste",
                "rep@estudante.sesisenai.org.br",
                "senha123",
                TypeUser.REPRESENTATIVE,
                null,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("deve retornar alunos e representantes de turma ativa")
    void shouldReturnStudentsAndRepresentatives() {
        ClassMembershipEntity studentMembership = new ClassMembershipEntity(student, classEntity, ClassRole.STUDENT);
        ClassMembershipEntity repMembership = new ClassMembershipEntity(representative, classEntity, ClassRole.REPRESENTATIVE);

        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.of(classEntity));
        when(membershipRepository.findActiveStudentsByClassId(classId, EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE)))
                .thenReturn(List.of(studentMembership, repMembership));

        List<ClassMembershipEntity> result = useCase.execute(classId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(m -> m.getClassRole())
                .containsExactlyInAnyOrder(ClassRole.STUDENT, ClassRole.REPRESENTATIVE);

        verify(classRepository).findByIdAndDeletedAtIsNull(classId);
        verify(membershipRepository).findActiveStudentsByClassId(classId, EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE));
    }

    @Test
    @DisplayName("deve retornar lista vazia quando turma não possui alunos elegíveis")
    void shouldReturnEmptyListWhenNoStudents() {
        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.of(classEntity));
        when(membershipRepository.findActiveStudentsByClassId(classId, EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE)))
                .thenReturn(List.of());

        List<ClassMembershipEntity> result = useCase.execute(classId);

        assertThat(result).isEmpty();
        verify(membershipRepository).findActiveStudentsByClassId(classId, EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE));
    }

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma não existe")
    void shouldThrowWhenClassNotFound() {
        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(ClassEntityNotFoundException.class);

        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma está desativada")
    void shouldThrowWhenClassIsDeleted() {
        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(ClassEntityNotFoundException.class);

        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando classId é nulo")
    void shouldThrowWhenClassIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(classRepository, membershipRepository);
    }

    @Test
    @DisplayName("não deve retornar docentes da turma")
    void shouldNotReturnTeachers() {
        UserEntity teacher = UserEntity.create(
                "Professor Teste",
                "professor@edu.sc.senai.br",
                "senha123",
                TypeUser.TEACHER,
                null,
                passwordEncoder
        );
        ClassMembershipEntity studentMembership = new ClassMembershipEntity(student, classEntity, ClassRole.STUDENT);

        when(classRepository.findByIdAndDeletedAtIsNull(classId)).thenReturn(Optional.of(classEntity));
        when(membershipRepository.findActiveStudentsByClassId(classId, EnumSet.of(ClassRole.STUDENT, ClassRole.REPRESENTATIVE)))
                .thenReturn(List.of(studentMembership));

        List<ClassMembershipEntity> result = useCase.execute(classId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClassRole()).isEqualTo(ClassRole.STUDENT);
    }
}