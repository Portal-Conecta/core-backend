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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

/** Popula o banco de desenvolvimento com a massa usada na apresentacao do Portal Conecta. */
@Configuration
@Profile("dev")
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String EMAIL_ADMIN = "admin@portal.test";

    private static final Map<String, UUID> COURSE_IDS = Map.ofEntries(
            Map.entry("MI", UUID.fromString("00000000-0000-0000-0000-000000000001")),
            Map.entry("MT", UUID.fromString("00000000-0000-0000-0000-000000000002")),
            Map.entry("WU", UUID.fromString("00000000-0000-0000-0000-000000000003")),
            Map.entry("ME", UUID.fromString("00000000-0000-0000-0000-000000000004")),
            Map.entry("MA", UUID.fromString("00000000-0000-0000-0000-000000000005")),
            Map.entry("MM", UUID.fromString("00000000-0000-0000-0000-000000000006")),
            Map.entry("WME", UUID.fromString("00000000-0000-0000-0000-000000000007")),
            Map.entry("WQ", UUID.fromString("00000000-0000-0000-0000-000000000008")),
            Map.entry("MF", UUID.fromString("00000000-0000-0000-0000-000000000009")),
            Map.entry("MQ", UUID.fromString("00000000-0000-0000-0000-000000000010")),
            Map.entry("WM", UUID.fromString("00000000-0000-0000-0000-000000000011")),
            Map.entry("WA", UUID.fromString("00000000-0000-0000-0000-000000000012"))
    );

    private static final Map<String, UUID> CLASS_IDS = Map.ofEntries(
            Map.entry("MI78", UUID.fromString("00000000-0000-0000-0000-000000000101")),
            Map.entry("MI77", UUID.fromString("00000000-0000-0000-0000-000000000102")),
            Map.entry("MT78", UUID.fromString("00000000-0000-0000-0000-000000000103")),
            Map.entry("MT77", UUID.fromString("00000000-0000-0000-0000-000000000104")),
            Map.entry("WU79", UUID.fromString("00000000-0000-0000-0000-000000000105")),
            Map.entry("WU78", UUID.fromString("00000000-0000-0000-0000-000000000106")),
            Map.entry("ME78", UUID.fromString("00000000-0000-0000-0000-000000000107")),
            Map.entry("ME77", UUID.fromString("00000000-0000-0000-0000-000000000108")),
            Map.entry("MA78", UUID.fromString("00000000-0000-0000-0000-000000000109")),
            Map.entry("MA77", UUID.fromString("00000000-0000-0000-0000-000000000110")),
            Map.entry("MM78", UUID.fromString("00000000-0000-0000-0000-000000000111")),
            Map.entry("MM77", UUID.fromString("00000000-0000-0000-0000-000000000112")),
            Map.entry("WME78", UUID.fromString("00000000-0000-0000-0000-000000000113")),
            Map.entry("WME77", UUID.fromString("00000000-0000-0000-0000-000000000114")),
            Map.entry("WQ77", UUID.fromString("00000000-0000-0000-0000-000000000115")),
            Map.entry("MF78", UUID.fromString("00000000-0000-0000-0000-000000000116")),
            Map.entry("MF77", UUID.fromString("00000000-0000-0000-0000-000000000117")),
            Map.entry("MQ78", UUID.fromString("00000000-0000-0000-0000-000000000118")),
            Map.entry("WM77", UUID.fromString("00000000-0000-0000-0000-000000000119")),
            Map.entry("WA77", UUID.fromString("00000000-0000-0000-0000-000000000120"))
    );

    private static final List<CourseSeed> COURSES = List.of(
            new CourseSeed("MI", "Aprendizagem Industrial em Desenvolvimento de Sistemas"),
            new CourseSeed("MT", "Aprendizagem Técnica em Eletrotécnica"),
            new CourseSeed("WU", "Aprendizagem Industrial em Operador de Usinagem"),
            new CourseSeed("ME", "Aprendizagem Técnica em Eletrônica"),
            new CourseSeed("MA", "Aprendizagem Técnica em Cibersistemas para Automação"),
            new CourseSeed("MM", "Aprendizagem Técnica em Manutenção de Máquinas Industriais"),
            new CourseSeed("WME", "Aprendizagem Industrial de Operador em Montagem de Produtos Eletroeletrônicos"),
            new CourseSeed("WQ", "Aprendizagem Industrial de Operador em Tintas e Vernizes"),
            new CourseSeed("MF", "Aprendizagem Técnica em Mecânica"),
            new CourseSeed("MQ", "Aprendizagem Técnica em Química"),
            new CourseSeed("WM", "Aprendizagem Industrial de Operador em Eletromecânica"),
            new CourseSeed("WA", "Aprendizagem Industrial em Assistente de Análise de Dados")
    );

    private static final List<ClassSeed> CLASSES = List.of(
            new ClassSeed("MI", 78), new ClassSeed("MI", 77), new ClassSeed("MT", 78), new ClassSeed("MT", 77),
            new ClassSeed("WU", 79), new ClassSeed("WU", 78), new ClassSeed("ME", 78), new ClassSeed("ME", 77),
            new ClassSeed("MA", 78), new ClassSeed("MA", 77), new ClassSeed("MM", 78), new ClassSeed("MM", 77),
            new ClassSeed("WME", 78), new ClassSeed("WME", 77), new ClassSeed("WQ", 77), new ClassSeed("MF", 78),
            new ClassSeed("MF", 77), new ClassSeed("MQ", 78), new ClassSeed("WM", 77), new ClassSeed("WA", 77)
    );

    private static final List<UserSeed> MI_TEACHERS = List.of(
            new UserSeed("Lucas SS", "lucas.ss@edu.sc.senai.br"),
            new UserSeed("João Valentim", "joao.valentim@edu.sc.senai.br"),
            new UserSeed("Kristian Erdmann", "kristian.erdmann@edu.sc.senai.br"),
            new UserSeed("C. Andrade", "c.andrade@edu.sc.senai.br")
    );

    private static final List<UserSeed> MI78_STUDENTS = List.of(
            new UserSeed("Daniel Muller", "daniel_muller150@estudante.sesisenai.org.br"),
            new UserSeed("Eduarda Ferrazza Stein", "eduarda_stein@estudante.sesisenai.org.br"),
            new UserSeed("Gustavo da Silva", "gustavo_silva159@estudante.sesisenai.org.br"),
            new UserSeed("Gustavo Rafael Kotryk", "gustavo_kotryk@estudante.sesisenai.org.br"),
            new UserSeed("Igor Tayson Bresolin Savero", "igor_savero@estudante.sesisenai.org.br"),
            new UserSeed("Jonathan Luis Uber", "jonathan_uber@estudante.sesisenai.org.br"),
            new UserSeed("Kauã Felix da Silva Costa", "kaua_fs_costa@estudante.sesisenai.org.br"),
            new UserSeed("Letícia Emanuele Güths", "leticia_guths@estudante.sesisenai.org.br"),
            new UserSeed("Lorhan Pierre de Melo", "lorhan_p_melo@estudante.sesisenai.org.br"),
            new UserSeed("Lucas Ismael Eckert", "lucas_eckert@estudante.sesisenai.org.br"),
            new UserSeed("Luiz Guilherme Fauro Ortiz", "luiz_gf_ortiz@estudante.sesisenai.org.br"),
            new UserSeed("Matheus Engel", "matheus_engel@estudante.sesisenai.org.br"),
            new UserSeed("Nícollas Gabriel Bartko de França", "nicollas_franca@estudante.sesisenai.org.br"),
            new UserSeed("Pablo Ruan Tzeliks", "pablo_tzeliks@estudante.sesisenai.org.br"),
            new UserSeed("Sara Soares dos Santos", "sara_soares-santos@estudante.sesisenai.org.br"),
            new UserSeed("Victor Daniel Strelow", "victor_strelow@estudante.sesisenai.org.br"),
            new UserSeed("Victória Nicoladelli", "victoria_nicolade@estudante.sesisenai.org.br"),
            new UserSeed("Vinícius de Figueiredo Anacleto", "vinicius_anacleto@estudante.sesisenai.org.br"),
            new UserSeed("Vinicius dos Santos Zapella", "vinicius_zapella@estudante.sesisenai.org.br")
    );

    private static final List<UserSeed> MI77_STUDENTS = List.of(
            new UserSeed("Gabrielli Glowatski", "gabrielli_glowatski@estudante.sesisenai.org.br"),
            new UserSeed("José A. Torres", "jose_a_torres@estudante.sesisenai.org.br"),
            new UserSeed("Ana B. O. Ribeiro", "ana_bo_ribeiro@estudante.sesisenai.org.br"),
            new UserSeed("André L. M. Pereira", "andre_lm_pereira@estudante.sesisenai.org.br"),
            new UserSeed("Catarina Klein", "catarina_klein@estudante.sesisenai.org.br"),
            new UserSeed("Daniel Sismer", "daniel_sismer@estudante.sesisenai.org.br"),
            new UserSeed("Eduardo D. Maia", "eduardo_d_maia@estudante.sesisenai.org.br"),
            new UserSeed("Elis Jasper", "elis_jasper@estudante.sesisenai.org.br"),
            new UserSeed("Emanuelle Hostin", "emanuelle_hostin@estudante.sesisenai.org.br"),
            new UserSeed("Gabriel E. Fagundes", "gabriel_e_fagundes@estudante.sesisenai.org.br"),
            new UserSeed("Hugo Paim", "hugo_paim@estudante.sesisenai.org.br"),
            new UserSeed("Kael Araújo", "kael_araujo@estudante.sesisenai.org.br"),
            new UserSeed("Leandro F. Lima", "leandro_f_lima@estudante.sesisenai.org.br"),
            new UserSeed("Lucas Schlei", "lucas_schlei@estudante.sesisenai.org.br"),
            new UserSeed("Maria E. Zabel", "maria_e_zabel@estudante.sesisenai.org.br"),
            new UserSeed("Matheus A. Castro", "matheus_a_castro@estudante.sesisenai.org.br"),
            new UserSeed("Melissa R. Pereira", "melissa_r_pereira@estudante.sesisenai.org.br"),
            new UserSeed("Murilo Kerschbaum", "murilo_kerschbaum@estudante.sesisenai.org.br")
    );

    private static final List<RoomSeed> ACTIVE_ROOMS = List.of(
            new RoomSeed("00000000-0000-0000-0000-000000000101", 101, TypeRoom.LABORATORY),
            new RoomSeed("00000000-0000-0000-0000-000000000102", 102, TypeRoom.LABORATORY),
            new RoomSeed("00000000-0000-0000-0000-000000000103", 103, TypeRoom.LABORATORY),
            new RoomSeed("00000000-0000-0000-0000-000000000109", 109, TypeRoom.LABORATORY),
            new RoomSeed("00000000-0000-0000-0000-000000000110", 110, TypeRoom.LABORATORY),
            new RoomSeed("00000000-0000-0000-0000-000000000201", 201, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000202", 202, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000203", 203, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000204", 204, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000205", 205, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000206", 206, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000207", 207, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000212", 212, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000213", 213, TypeRoom.CLASSROOM),
            new RoomSeed("00000000-0000-0000-0000-000000000214", 214, TypeRoom.CLASSROOM)
    );

    @Bean
    public CommandLineRunner seedDevData(
            UserRepository users, CourseRepository courses, ClassRepository classes,
            ClassMembershipRepository memberships, RoomRepository rooms,
            PasswordEncoder passwordEncoder, TransactionTemplate transactionTemplate
    ) {
        return args -> transactionTemplate.executeWithoutResult(status ->
                seedPresentationData(users, courses, classes, memberships, rooms, passwordEncoder));
    }

    void seedPresentationData(
            UserRepository users, CourseRepository courses, ClassRepository classes,
            ClassMembershipRepository memberships, RoomRepository rooms, PasswordEncoder passwordEncoder
    ) {
        UserEntity admin = findOrCreateUser(users, passwordEncoder, "Admin Portal", EMAIL_ADMIN, TypeUser.ADMIN, null);
        findOrCreateUser(users, passwordEncoder, "Bruna Meinerz", "bruna.meinerz@sc.senai.br", TypeUser.SENAI, admin);
        findOrCreateUser(users, passwordEncoder, "Nathalya", "nathalya@weg.net", TypeUser.WEG, admin);

        Map<String, CourseEntity> courseByCode = new LinkedHashMap<>();
        COURSES.forEach(seed -> courseByCode.put(seed.code(), findOrCreateCourse(courses, seed, admin)));
        Map<String, ClassEntity> classByKey = new LinkedHashMap<>();
        CLASSES.forEach(seed -> classByKey.put(seed.key(), findOrCreateClass(classes, seed, courseByCode.get(seed.courseCode()), admin)));

        ClassEntity mi77 = classByKey.get("MI77");
        ClassEntity mi78 = classByKey.get("MI78");
        MI_TEACHERS.forEach(seed -> {
            UserEntity teacher = findOrCreateUser(users, passwordEncoder, seed.name(), seed.email(), TypeUser.TEACHER, admin);
            findOrCreateMembership(memberships, teacher, mi77, ClassRole.TEACHER);
            findOrCreateMembership(memberships, teacher, mi78, ClassRole.TEACHER);
        });

        MI78_STUDENTS.forEach(seed -> {
            boolean representative = seed.email().equals("lucas_eckert@estudante.sesisenai.org.br")
                    || seed.email().equals("leticia_guths@estudante.sesisenai.org.br");
            UserEntity student = findOrCreateUser(users, passwordEncoder, seed.name(), seed.email(),
                    representative ? TypeUser.REPRESENTATIVE : TypeUser.STUDENT, admin);
            findOrCreateMembership(memberships, student, mi78,
                    representative ? ClassRole.REPRESENTATIVE : ClassRole.STUDENT);
        });

        MI77_STUDENTS.forEach(seed -> {
            boolean representative = seed.email().equals("elis_jasper@estudante.sesisenai.org.br")
                    || seed.email().equals("gabriel_e_fagundes@estudante.sesisenai.org.br");
            UserEntity student = findOrCreateUser(users, passwordEncoder, seed.name(), seed.email(),
                    representative ? TypeUser.REPRESENTATIVE : TypeUser.STUDENT, admin);
            findOrCreateMembership(memberships, student, mi77,
                    representative ? ClassRole.REPRESENTATIVE : ClassRole.STUDENT);
        });

        CLASSES.stream().filter(seed -> !seed.key().equals("MI77") && !seed.key().equals("MI78"))
                .forEach(seed -> seedDemoMembers(users, memberships, passwordEncoder, admin, classByKey.get(seed.key())));

        seedActiveRooms(rooms, admin);
    }

    private void seedDemoMembers(UserRepository users, ClassMembershipRepository memberships,
            PasswordEncoder passwordEncoder, UserEntity admin, ClassEntity classEntity) {
        String key = classEntity.getName().toLowerCase();
        UserEntity firstRepresentative = findOrCreateUser(users, passwordEncoder, "Representante 1 " + classEntity.getName(),
                "representante.1." + key + "@estudante.sesisenai.org.br", TypeUser.REPRESENTATIVE, admin);
        UserEntity secondRepresentative = findOrCreateUser(users, passwordEncoder, "Representante 2 " + classEntity.getName(),
                "representante.2." + key + "@estudante.sesisenai.org.br", TypeUser.REPRESENTATIVE, admin);
        UserEntity student = findOrCreateUser(users, passwordEncoder, "Aluno " + classEntity.getName(),
                "aluno." + key + "@estudante.sesisenai.org.br", TypeUser.STUDENT, admin);
        findOrCreateMembership(memberships, firstRepresentative, classEntity, ClassRole.REPRESENTATIVE);
        findOrCreateMembership(memberships, secondRepresentative, classEntity, ClassRole.REPRESENTATIVE);
        findOrCreateMembership(memberships, student, classEntity, ClassRole.STUDENT);
    }

    private UserEntity findOrCreateUser(UserRepository users, PasswordEncoder encoder, String name,
            String email, TypeUser type, UserEntity createdBy) {
        if (users.existsByEmailIgnoreCase(email)) {
            return users.findAll().stream().filter(user -> user.getEmail().equalsIgnoreCase(email)).findFirst().orElseThrow();
        }
        return users.save(UserEntity.create(name, email, DEFAULT_PASSWORD, type, createdBy, encoder));
    }

    private CourseEntity findOrCreateCourse(CourseRepository courses, CourseSeed seed, UserEntity admin) {
        courses.findAll().stream().filter(course -> course.getCode().equalsIgnoreCase(seed.code())).findFirst().orElseGet(() -> {
            CourseEntity course = CourseEntity.create(seed.name(), seed.code());
            course.setCreatedBy(admin);
            course.setUpdatedBy(admin);
            return courses.save(course);
        });
        courses.updateIdByCode(COURSE_IDS.get(seed.code()), seed.code());
        return courses.findByCodeAndDeletedAtIsNull(seed.code()).orElseThrow();
    }

    private ClassEntity findOrCreateClass(ClassRepository classes, ClassSeed seed, CourseEntity course, UserEntity admin) {
        int number = seed.number();
        String name = course.getCode() + number;
        classes.findAll().stream().filter(classEntity -> classEntity.getName().equalsIgnoreCase(name)).findFirst()
                .orElseGet(() -> classes.save(ClassEntity.create(Shift.FULL_AM_PM, number, course, admin)));
        classes.updateIdByName(CLASS_IDS.get(seed.key()), name);
        return classes.findAll().stream().filter(classEntity -> classEntity.getName().equalsIgnoreCase(name)).findFirst().orElseThrow();
    }

    private void findOrCreateMembership(ClassMembershipRepository memberships, UserEntity user,
            ClassEntity classEntity, ClassRole role) {
        if (!memberships.existsByUserIdAndClassId(user.getId(), classEntity.getId())) {
            memberships.save(new ClassMembershipEntity(user, classEntity, role));
        }
    }

    private RoomEntity findOrCreateRoom(RoomRepository rooms, int number, TypeRoom type, UserEntity admin) {
        return rooms.findAll().stream().filter(room -> room.getNumber().equals(number)).findFirst()
                .orElseGet(() -> rooms.save(RoomEntity.create(number, type, admin)));
    }

    void seedActiveRooms(RoomRepository rooms, UserEntity admin) {
        ACTIVE_ROOMS.forEach(room -> {
            findOrCreateRoom(rooms, room.number(), room.type(), admin);
            rooms.updateIdByNumber(room.id(), room.number());
        });
    }

    private record CourseSeed(String code, String name) { }
    private record ClassSeed(String courseCode, int number) { private String key() { return courseCode + number; } }
    private record UserSeed(String name, String email) { }
    private record RoomSeed(UUID id, int number, TypeRoom type) {
        private RoomSeed(String id, int number, TypeRoom type) { this(UUID.fromString(id), number, type); }
    }
}
