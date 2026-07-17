package com.portal.conecta.hub.shared.config;

import com.portal.conecta.hub.module.classes.domain.model.*;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Popula o banco de dados com massa de dados mockados para o perfil {@code dev}.
 *
 * <p>Executa uma única vez na inicialização da aplicação via {@link org.springframework.boot.CommandLineRunner}.
 * Todas as operações são idempotentes: usuários, cursos, turmas, vínculos e salas são criados
 * apenas se ainda não existirem, identificados por e-mail, código ou número fixos.
 *
 * <p>Inclui usuários ativos de todos os tipos ({@code ADMIN}, {@code SENAI}, {@code WEG},
 * {@code TEACHER}, {@code STUDENT}, {@code REPRESENTATIVE}), usuários inativos,
 * cursos, turmas ativas e desativadas, vínculos acadêmicos e salas com soft delete aplicado.
 *
 * <p><strong>Ativo apenas no perfil {@code dev}. Não deve ser executado em produção.</strong>
 */
@Configuration
@Profile("dev")
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private static final String DEFAULT_PASSWORD = "123456";

    // --- Emails fixos para idempotência ---

    // ADMIN (1)
    private static final String EMAIL_ADMIN = "admin@portal.test";

    // SENAI (3)
    private static final String EMAIL_SENAI_1 = "ana.costa@sc.senai.br";
    private static final String EMAIL_SENAI_2 = "bruno.lima@sc.senai.br";
    private static final String EMAIL_SENAI_3 = "carla.souza@sc.senai.br";

    // WEG (3)
    private static final String EMAIL_WEG_1 = "daniel.melo@weg.net";
    private static final String EMAIL_WEG_2 = "fernanda.dias@weg.net";
    private static final String EMAIL_WEG_3 = "gustavo.reis@weg.net";

    // TEACHER (3)
    private static final String EMAIL_TEACHER_1 = "docente.principal@edu.sc.senai.br";
    private static final String EMAIL_TEACHER_2 = "docente.secundario@edu.sc.senai.br";
    private static final String EMAIL_TEACHER_3 = "docente.terciario@edu.sc.senai.br";

    // STUDENT (3)
    private static final String EMAIL_STUDENT_1 = "aluno.padrao@estudante.sesisenai.org.br";
    private static final String EMAIL_STUDENT_2 = "aluno.dois@estudante.sesisenai.org.br";
    private static final String EMAIL_STUDENT_3 = "aluno.tres@estudante.sesisenai.org.br";

    // REPRESENTATIVE (3)
    private static final String EMAIL_REPRESENTATIVE_1 = "representante.turma@estudante.sesisenai.org.br";
    private static final String EMAIL_REPRESENTATIVE_2 = "representante.dois@estudante.sesisenai.org.br";
    private static final String EMAIL_REPRESENTATIVE_3 = "representante.tres@estudante.sesisenai.org.br";

    // INACTIVE (3)
    private static final String EMAIL_INACTIVE_1 = "aluno.inativo@estudante.sesisenai.org.br";
    private static final String EMAIL_INACTIVE_2 = "docente.inativo@edu.sc.senai.br";
    private static final String EMAIL_INACTIVE_3 = "senai.inativo@sc.senai.br";

    // --- Códigos de curso ---
    private static final String CODE_MIDS  = "MIDS";
    private static final String CODE_ADSIS = "ADSIS";

    // --- Números de sala ---
    private static final int ROOM_CLASSROOM_NUMBER  = 101;
    private static final int ROOM_LABORATORY_NUMBER = 201;
    private static final int ROOM_DELETED_NUMBER    = 301;

    @Bean
    public CommandLineRunner seedDevData(
            UserRepository userRepository,
            CourseRepository courseRepository,
            ClassRepository classRepository,
            ClassMembershipRepository classMembershipRepository,
            RoomRepository roomRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            log.info("[DEV SEED] Iniciando população de dados mockados...");

            // ----------------------------------------------------------------
            // 1. USUÁRIOS
            // ----------------------------------------------------------------

            UserEntity admin = findOrCreateUser(
                    userRepository, passwordEncoder,
                    "Admin Portal", EMAIL_ADMIN, TypeUser.ADMIN
            );

            // SENAI
            findOrCreateUser(userRepository, passwordEncoder, "Ana Costa",     EMAIL_SENAI_1, TypeUser.SENAI);
            findOrCreateUser(userRepository, passwordEncoder, "Bruno Lima",    EMAIL_SENAI_2, TypeUser.SENAI);
            findOrCreateUser(userRepository, passwordEncoder, "Carla Souza",   EMAIL_SENAI_3, TypeUser.SENAI);

            // WEG
            findOrCreateUser(userRepository, passwordEncoder, "Daniel Melo",   EMAIL_WEG_1, TypeUser.WEG);
            findOrCreateUser(userRepository, passwordEncoder, "Fernanda Dias", EMAIL_WEG_2, TypeUser.WEG);
            findOrCreateUser(userRepository, passwordEncoder, "Gustavo Reis",  EMAIL_WEG_3, TypeUser.WEG);

            // TEACHER
            UserEntity teacher1 = findOrCreateUser(
                    userRepository, passwordEncoder, "Docente Principal",  EMAIL_TEACHER_1, TypeUser.TEACHER
            );
            UserEntity teacher2 = findOrCreateUser(
                    userRepository, passwordEncoder, "Docente Secundário", EMAIL_TEACHER_2, TypeUser.TEACHER
            );
            UserEntity teacher3 = findOrCreateUser(
                    userRepository, passwordEncoder, "Docente Terciário",  EMAIL_TEACHER_3, TypeUser.TEACHER
            );

            // STUDENT
            UserEntity student1 = findOrCreateUser(
                    userRepository, passwordEncoder, "Aluno Padrão", EMAIL_STUDENT_1, TypeUser.STUDENT
            );
            UserEntity student2 = findOrCreateUser(
                    userRepository, passwordEncoder, "Aluno Dois",   EMAIL_STUDENT_2, TypeUser.STUDENT
            );
            UserEntity student3 = findOrCreateUser(
                    userRepository, passwordEncoder, "Aluno Três",   EMAIL_STUDENT_3, TypeUser.STUDENT
            );

            // REPRESENTATIVE
            UserEntity representative1 = findOrCreateUser(
                    userRepository, passwordEncoder, "Representante Turma", EMAIL_REPRESENTATIVE_1, TypeUser.REPRESENTATIVE
            );
            UserEntity representative2 = findOrCreateUser(
                    userRepository, passwordEncoder, "Representante Dois",  EMAIL_REPRESENTATIVE_2, TypeUser.REPRESENTATIVE
            );
            UserEntity representative3 = findOrCreateUser(
                    userRepository, passwordEncoder, "Representante Três",  EMAIL_REPRESENTATIVE_3, TypeUser.REPRESENTATIVE
            );

            // INACTIVE
            findOrCreateInactiveUser(userRepository, passwordEncoder, "Aluno Inativo",   EMAIL_INACTIVE_1, TypeUser.STUDENT);
            findOrCreateInactiveUser(userRepository, passwordEncoder, "Docente Inativo", EMAIL_INACTIVE_2, TypeUser.TEACHER);
            findOrCreateInactiveUser(userRepository, passwordEncoder, "SENAI Inativo",   EMAIL_INACTIVE_3, TypeUser.SENAI);

            // ----------------------------------------------------------------
            // 2. CURSOS
            // ----------------------------------------------------------------
            CourseEntity mids = findOrCreateCourse(
                    courseRepository, "Manutenção e Infraestrutura de Redes", CODE_MIDS
            );

            CourseEntity adsis = findOrCreateCourse(
                    courseRepository, "Análise e Desenvolvimento de Sistemas", CODE_ADSIS
            );

            // ----------------------------------------------------------------
            // 3. TURMAS
            // ----------------------------------------------------------------

            // MIDS1 — turma principal: alunos, representante e docente principal
            ClassEntity classMids1 = findOrCreateClass(
                    classRepository, mids, Shift.FULL_AM_PM, 1, admin
            );

            // ADSIS1 — segunda turma do docente principal
            ClassEntity classAdsis1 = findOrCreateClass(
                    classRepository, adsis, Shift.FULL_PM_NT, 1, admin
            );

            // MIDS2 — turma do docente secundário e representante 2
            ClassEntity classMids2 = findOrCreateClass(
                    classRepository, mids, Shift.FULL_PM_NT, 2, admin
            );

            // ADSIS2 — turma do docente terciário e representante 3
            ClassEntity classAdsis2 = findOrCreateClass(
                    classRepository, adsis, Shift.FULL_AM_PM, 2, admin
            );

            // ADSIS99 — turma desativada para testar filtros padrão
            ClassEntity classInactive = findOrCreateClass(
                    classRepository, adsis, Shift.FULL_AM_PM, 99, admin
            );
            if (classInactive.isActive()) {
                classInactive.deactivate(admin);
                classRepository.save(classInactive);
                log.info("[DEV SEED] Turma {} desativada.", classInactive.getName());
            }

            // ----------------------------------------------------------------
            // 4. VÍNCULOS ACADÊMICOS
            // ----------------------------------------------------------------

            // MIDS1: student1, representative1, teacher1
            // — cobre Checklist, Mapa de Sala e Comunicados
            findOrCreateMembership(classMembershipRepository, student1,        classMids1,  ClassRole.STUDENT);
            findOrCreateMembership(classMembershipRepository, representative1, classMids1,  ClassRole.REPRESENTATIVE);
            findOrCreateMembership(classMembershipRepository, teacher1,        classMids1,  ClassRole.TEACHER);

            // ADSIS1: student2, teacher1 (docente em duas turmas ativas)
            findOrCreateMembership(classMembershipRepository, student2,        classAdsis1, ClassRole.STUDENT);
            findOrCreateMembership(classMembershipRepository, teacher1,        classAdsis1, ClassRole.TEACHER);

            // MIDS2: student3, representative2, teacher2
            findOrCreateMembership(classMembershipRepository, student3,        classMids2,  ClassRole.STUDENT);
            findOrCreateMembership(classMembershipRepository, representative2, classMids2,  ClassRole.REPRESENTATIVE);
            findOrCreateMembership(classMembershipRepository, teacher2,        classMids2,  ClassRole.TEACHER);

            // ADSIS2: student1, representative3, teacher3
            findOrCreateMembership(classMembershipRepository, student1,        classAdsis2, ClassRole.STUDENT);
            findOrCreateMembership(classMembershipRepository, representative3, classAdsis2, ClassRole.REPRESENTATIVE);
            findOrCreateMembership(classMembershipRepository, teacher3,        classAdsis2, ClassRole.TEACHER);

            // ----------------------------------------------------------------
            // 5. SALAS
            // ----------------------------------------------------------------

            // Sala 101 — CLASSROOM ativa (referência para Mapa de Sala)
            findOrCreateRoom(roomRepository, ROOM_CLASSROOM_NUMBER,  TypeRoom.CLASSROOM,  admin);

            // Sala 201 — LABORATORY ativa
            findOrCreateRoom(roomRepository, ROOM_LABORATORY_NUMBER, TypeRoom.LABORATORY, admin);

            // Sala 301 — removida (soft delete) para testar filtros padrão
            RoomEntity deletedRoom = findOrCreateRoom(
                    roomRepository, ROOM_DELETED_NUMBER, TypeRoom.OTHER, admin
            );
            if (deletedRoom.isActive()) {
                deletedRoom.delete(admin);
                roomRepository.save(deletedRoom);
                log.info("[DEV SEED] Sala {} marcada como removida.", deletedRoom.getNumber());
            }

            log.info("[DEV SEED] Massa de dados mockados disponível.");
        };
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private UserEntity findOrCreateUser(
            UserRepository repo,
            PasswordEncoder encoder,
            String name,
            String email,
            TypeUser type
    ) {
        if (repo.existsByEmailIgnoreCase(email)) {
            return repo.findAll().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "[DEV SEED] Usuário com email " + email + " não encontrado após verificação de existência."
                    ));
        }

        UserEntity user = UserEntity.create(name, email, DEFAULT_PASSWORD, type, null, encoder);
        UserEntity saved = repo.save(user);
        log.info("[DEV SEED] Usuário criado: {} ({})", email, type);
        return saved;
    }

    private void findOrCreateInactiveUser(
            UserRepository repo,
            PasswordEncoder encoder,
            String name,
            String email,
            TypeUser type
    ) {
        if (repo.existsByEmailIgnoreCase(email)) {
            return;
        }

        UserEntity user = UserEntity.create(name, email, DEFAULT_PASSWORD, type, null, encoder);
        user.delete(null);
        repo.save(user);
        log.info("[DEV SEED] Usuário inativo criado: {}", email);
    }

    private CourseEntity findOrCreateCourse(
            CourseRepository repo,
            String name,
            String code
    ) {
        return repo.findAll().stream()
                .filter(c -> c.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseGet(() -> {
                    CourseEntity course = CourseEntity.create(name, code);
                    CourseEntity saved = repo.save(course);
                    log.info("[DEV SEED] Curso criado: {} ({})", code, name);
                    return saved;
                });
    }

    private ClassEntity findOrCreateClass(
            ClassRepository repo,
            CourseEntity course,
            Shift shift,
            int number,
            UserEntity createdBy
    ) {
        String expectedName = course.getCode() + number;
        return repo.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(expectedName))
                .findFirst()
                .orElseGet(() -> {
                    ClassEntity cls = ClassEntity.create(shift, number, course, createdBy);
                    ClassEntity saved = repo.save(cls);
                    log.info("[DEV SEED] Turma criada: {}", saved.getName());
                    return saved;
                });
    }

    private void findOrCreateMembership(
            ClassMembershipRepository repo,
            UserEntity user,
            ClassEntity cls,
            ClassRole role
    ) {
        if (repo.existsByUserIdAndClassId(user.getId(), cls.getId())) {
            return;
        }
        ClassMembershipEntity membership = new ClassMembershipEntity(user, cls, role);
        repo.save(membership);
        log.info("[DEV SEED] Vínculo criado: {} -> {} ({})", user.getEmail(), cls.getName(), role);
    }

    private RoomEntity findOrCreateRoom(
            RoomRepository repo,
            int number,
            TypeRoom type,
            UserEntity createdBy
    ) {
        return repo.findAll().stream()
                .filter(r -> r.getNumber().equals(number))
                .findFirst()
                .orElseGet(() -> {
                    RoomEntity room = RoomEntity.create(number, type, createdBy);
                    RoomEntity saved = repo.save(room);
                    log.info("[DEV SEED] Sala criada: {} ({})", number, type);
                    return saved;
                });
    }
}
