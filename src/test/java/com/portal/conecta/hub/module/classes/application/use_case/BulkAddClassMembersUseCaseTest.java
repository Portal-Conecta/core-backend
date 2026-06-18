package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.BulkAddMembersCommand;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassMembershipValidator;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkAddClassMembersUseCaseTest {

    @Mock
    private RequestContextProvider requestProvider;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClassMembershipRepository membershipRepository;

    @InjectMocks
    private BulkAddClassMembersUseCase useCase;

    private UUID executorId;
    private UUID classId;
    private UserEntity executor;
    private UserEntity studentA;
    private UserEntity studentB;
    private UserEntity teacher;
    private ClassEntity classEntity;
    private RequestContext senaiContext;

    @BeforeEach
    void setUp() {
        executorId = UUID.randomUUID();
        classId = UUID.randomUUID();

        executor = new UserEntity("Executor", "executor@sc.senai.br", "hash", TypeUser.SENAI);
        studentA = new UserEntity("Aluno A", "alunoa@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        studentB = new UserEntity("Aluno B", "alunob@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        teacher  = new UserEntity("Professor", "professor@edu.sc.senai.br", "hash", TypeUser.TEACHER);

        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);
        ReflectionTestUtils.setField(classEntity, "id", classId); // <- adiciona essa linha

        senaiContext = new RequestContext(executorId, TypeUser.SENAI, List.of());
    }

    @Test
    @DisplayName("deve criar todos os vínculos quando todos os itens são válidos")
    void shouldCreateAllMembershipsWhenAllItemsAreValid() {
        UUID userIdA = UUID.randomUUID();
        UUID userIdB = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userIdA, ClassRole.STUDENT),
                new BulkAddMembersCommand.Item(userIdB, ClassRole.STUDENT)
        ));

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userIdA)).thenReturn(Optional.of(studentA));
        when(userRepository.findById(userIdB)).thenReturn(Optional.of(studentB));
        when(membershipRepository.existsByUserIdAndClassId(userIdA, classId)).thenReturn(false);
        when(membershipRepository.existsByUserIdAndClassId(userIdB, classId)).thenReturn(false);
        when(membershipRepository.countByUserIdAndClassRole(userIdA, ClassRole.STUDENT)).thenReturn(0L);
        when(membershipRepository.countByUserIdAndClassRole(userIdB, ClassRole.STUDENT)).thenReturn(0L);
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ClassMembershipEntity> result = useCase.execute(command);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassMembershipEntity::getClassRole)
                .containsOnly(ClassRole.STUDENT);
        verify(membershipRepository, times(2)).save(any(ClassMembershipEntity.class));
    }

    @Test
    @DisplayName("deve criar vínculo de aluno e docente na mesma requisição")
    void shouldCreateStudentAndTeacherInSameRequest() {
        UUID userIdStudent = UUID.randomUUID();
        UUID userIdTeacher = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userIdStudent, ClassRole.STUDENT),
                new BulkAddMembersCommand.Item(userIdTeacher, ClassRole.TEACHER)
        ));

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userIdStudent)).thenReturn(Optional.of(studentA));
        when(userRepository.findById(userIdTeacher)).thenReturn(Optional.of(teacher));
        when(membershipRepository.existsByUserIdAndClassId(userIdStudent, classId)).thenReturn(false);
        when(membershipRepository.existsByUserIdAndClassId(userIdTeacher, classId)).thenReturn(false);
        when(membershipRepository.countByUserIdAndClassRole(userIdStudent, ClassRole.STUDENT)).thenReturn(0L);
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ClassMembershipEntity> result = useCase.execute(command);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassMembershipEntity::getClassRole)
                .containsExactlyInAnyOrder(ClassRole.STUDENT, ClassRole.TEACHER);
        verify(membershipRepository, never()).countByUserIdAndClassRole(userIdTeacher, ClassRole.TEACHER);
    }
}