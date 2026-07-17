package com.portal.conecta.hub.module.notification.infrastructure.adapter;

import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand.CommandFilter;
import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand.CommandScope;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.NotificationFilterType;
import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.ClassScopeResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.CourseScopeResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.GlobalScopeResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.NotificationRecipientFilterResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.UserDirectResolver;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationRecipientPortAdapter")
class NotificationRecipientPortAdapterTest {

    @Mock private UserDirectResolver userDirectResolver;
    @Mock private ClassScopeResolver classScopeResolver;
    @Mock private CourseScopeResolver courseScopeResolver;
    @Mock private GlobalScopeResolver globalScopeResolver;
    @Mock private NotificationRecipientFilterResolver filterResolver;

    @InjectMocks
    private NotificationRecipientPortAdapter adapter;

    // ---------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------

    private NotificationEntity notification(UUID id) {
        NotificationEntity entity = new NotificationEntity(
                "msg-001", "corr-001", "SOURCE", "EVENT",
                Instant.now(), "Título", "Corpo", null
        );
        // seta o id via spy para simular entidade persistida
        NotificationEntity spy = spy(entity);
        doReturn(id).when(spy).getId();
        return spy;
    }

    private CommandScope scopeUser(String correlationId) {
        return new CommandScope(NotificationScopeType.USER, correlationId);
    }

    private CommandScope scopeClass(String correlationId) {
        return new CommandScope(NotificationScopeType.CLASS, correlationId);
    }

    private CommandScope scopeCourse(String correlationId) {
        return new CommandScope(NotificationScopeType.COURSE, correlationId);
    }

    private CommandScope scopeRoom(String correlationId) {
        return new CommandScope(NotificationScopeType.ROOM, correlationId);
    }

    private CommandScope scopeGlobal(String correlationId) {
        return new CommandScope(NotificationScopeType.GLOBAL, correlationId);
    }

    private CommandFilter filterRole(String value) {
        return new CommandFilter(NotificationFilterType.ROLE, value);
    }

    private CommandFilter filterShift(String value) {
        return new CommandFilter(NotificationFilterType.SHIFT, value);
    }

    @BeforeEach
    void setUp() {
        lenient().when(filterResolver.resolve(anyList())).thenAnswer(invocation ->
                new NotificationRecipientFilterResolver().resolve(invocation.getArgument(0))
        );
    }

    // ---------------------------------------------------------------
    // testes
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("roteamento de escopos")
    class Roteamento {

        @Test
        @DisplayName("escopo USER deve chamar userDirectResolver com o UUID correto")
        void escopoUserChamaUserDirectResolver() {
            UUID notifId = UUID.randomUUID();
            UUID userId  = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif, List.of(scopeUser(userId.toString())), List.of());

            verify(userDirectResolver).insert(notifId, Set.of(userId));
            // os outros dois são chamados, mas com listas vazias
            verify(classScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
            verify(courseScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
        }

        @Test
        @DisplayName("escopo CLASS deve chamar classScopeResolver com o UUID correto")
        void escopoClassChamaClassScopeResolver() {
            UUID notifId  = UUID.randomUUID();
            UUID classId  = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif, List.of(scopeClass(classId.toString())), List.of());

            verify(classScopeResolver).insert(notifId, List.of(classId),
                    EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
            verify(userDirectResolver).insert(eq(notifId), argThat(Set::isEmpty));
            verify(courseScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
        }

        @Test
        @DisplayName("escopo COURSE deve chamar courseScopeResolver com o UUID correto")
        void escopoCourseChama () {
            UUID notifId  = UUID.randomUUID();
            UUID courseId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif, List.of(scopeCourse(courseId.toString())), List.of());

            verify(courseScopeResolver).insert(notifId, List.of(courseId),
                    EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
            verify(userDirectResolver).insert(eq(notifId), argThat(Set::isEmpty));
            verify(classScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
        }

        @Test
        @DisplayName("escopo ROOM deve ser ignorado sem destinatarios adicionais")
        void escopoRoomSaoIgnorado() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeRoom(UUID.randomUUID().toString())),
                    List.of());

            verify(userDirectResolver).insert(eq(notifId), argThat(Set::isEmpty));
            verify(classScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
            verify(courseScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
            verifyNoInteractions(globalScopeResolver);
        }

        @Test
        @DisplayName("escopo GLOBAL deve chamar globalScopeResolver sem exigir correlationId")
        void escopoGlobalChamaGlobalScopeResolver() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif, List.of(scopeGlobal(null)), List.of());

            verify(globalScopeResolver).insert(notifId, EnumSet.noneOf(TypeUser.class));
            verify(userDirectResolver).insert(eq(notifId), argThat(Set::isEmpty));
            verify(classScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
            verify(courseScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
        }

        @Test
        @DisplayName("multiplos escopos GLOBAL devem chamar globalScopeResolver uma unica vez")
        void multiplosEscoposGlobalChamamResolverUmaVez() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif, List.of(scopeGlobal(null), scopeGlobal(UUID.randomUUID().toString())), List.of());

            verify(globalScopeResolver, times(1)).insert(notifId, EnumSet.noneOf(TypeUser.class));
        }

        @Test
        @DisplayName("múltiplos escopos do mesmo tipo devem ser agrupados")
        void multiplosEscoposDoMesmoTipoSaoAgrupados() {
            UUID notifId = UUID.randomUUID();
            UUID classId1 = UUID.randomUUID();
            UUID classId2 = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeClass(classId1.toString()), scopeClass(classId2.toString())),
                    List.of());

            verify(classScopeResolver).insert(notifId, List.of(classId1, classId2),
                    EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
        }

        @Test
        @DisplayName("escopos mistos devem ser roteados para os resolvers corretos")
        void escoposMistosSaoRoteadosCorretamente() {
            UUID notifId  = UUID.randomUUID();
            UUID userId   = UUID.randomUUID();
            UUID classId  = UUID.randomUUID();
            UUID courseId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(
                            scopeUser(userId.toString()),
                            scopeClass(classId.toString()),
                            scopeCourse(courseId.toString())
                    ),
                    List.of());

            verify(userDirectResolver).insert(notifId, Set.of(userId));
            verify(classScopeResolver).insert(notifId, List.of(classId),
                    EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
            verify(courseScopeResolver).insert(notifId, List.of(courseId),
                    EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
        }
    }

    @Nested
    @DisplayName("filtros ROLE")
    class FiltrosRole {

        @Test
        @DisplayName("filtro ROLE=STUDENT deve repassar STUDENT e REPRESENTATIVE")
        void filtroRoleStudentRepassaEnumSetCorreto() {
            UUID notifId = UUID.randomUUID();
            UUID classId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeClass(classId.toString())),
                    List.of(filterRole("STUDENT")));

            verify(classScopeResolver).insert(notifId, List.of(classId),
                    EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE), EnumSet.noneOf(Shift.class));
        }

        @Test
        @DisplayName("filtro ROLE deve restringir escopo GLOBAL")
        void filtroRoleAfetaEscopoGlobal() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeGlobal(null)),
                    List.of(filterRole("STUDENT")));

            verify(globalScopeResolver).insert(notifId, EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE));
        }

        @Test
        @DisplayName("múltiplos filtros ROLE devem ser combinados em um único EnumSet")
        void multiplosFiltrosRoleSaoCombinados() {
            UUID notifId  = UUID.randomUUID();
            UUID courseId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeCourse(courseId.toString())),
                    List.of(filterRole("STUDENT"), filterRole("TEACHER")));

            verify(courseScopeResolver).insert(
                    notifId,
                    List.of(courseId),
                    EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE, TypeUser.TEACHER),
                    EnumSet.noneOf(Shift.class)
            );
        }

        @Test
        @DisplayName("filtro ROLE com valor inválido deve lançar InvalidNotificationPayloadException")
        void filtroRoleInvalidoLancaExcecao() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            assertThatThrownBy(() ->
                    adapter.dispatch(notif,
                            List.of(scopeClass(UUID.randomUUID().toString())),
                            List.of(filterRole("INVALIDO")))
            )
                    .isInstanceOf(InvalidNotificationPayloadException.class)
                    .hasMessageContaining("INVALIDO");
        }

        @Test
        @DisplayName("sem filtros ROLE deve repassar EnumSet vazio para os resolvers")
        void semFiltroRoleRepassaEnumSetVazio() {
            UUID notifId = UUID.randomUUID();
            UUID classId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeClass(classId.toString())),
                    List.of());

            verify(classScopeResolver).insert(notifId, List.of(classId),
                    EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
        }

        @Test
        @DisplayName("filtros ROLE só afetam CLASS/COURSE — escopo USER não é filtrado por role")
        void filtroRoleNaoAfetaEscopoUser() {
            UUID notifId = UUID.randomUUID();
            UUID userId  = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            // mesmo com ROLE=TEACHER, o userDirectResolver deve receber o userId normalmente
            adapter.dispatch(notif,
                    List.of(scopeUser(userId.toString())),
                    List.of(filterRole("TEACHER")));

            verify(userDirectResolver).insert(notifId, Set.of(userId));
        }
    }

    @Nested
    @DisplayName("filtros SHIFT")
    class FiltrosShift {

        @Test
        @DisplayName("filtro SHIFT=FULL_AM_PM deve repassar EnumSet com FULL_AM_PM")
        void filtroShiftRepassaEnumSetCorreto() {
            UUID notifId = UUID.randomUUID();
            UUID classId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeClass(classId.toString())),
                    List.of(filterShift("FULL_AM_PM")));

            verify(classScopeResolver).insert(notifId, List.of(classId),
                    EnumSet.noneOf(TypeUser.class), EnumSet.of(Shift.FULL_AM_PM));
        }

        @Test
        @DisplayName("múltiplos filtros SHIFT devem ser combinados em um único EnumSet")
        void multiplosFiltrosShiftSaoCombinados() {
            UUID notifId  = UUID.randomUUID();
            UUID courseId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeCourse(courseId.toString())),
                    List.of(filterShift("FULL_AM_PM"), filterShift("FULL_PM_NT")));

            verify(courseScopeResolver).insert(
                    notifId,
                    List.of(courseId),
                    EnumSet.noneOf(TypeUser.class),
                    EnumSet.of(Shift.FULL_AM_PM, Shift.FULL_PM_NT)
            );
        }

        @Test
        @DisplayName("ROLE e SHIFT enviados juntos devem ser repassados combinados")
        void roleEShiftJuntosSaoRepassadosCombinados() {
            UUID notifId = UUID.randomUUID();
            UUID classId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeClass(classId.toString())),
                    List.of(filterRole("STUDENT"), filterShift("FULL_AM_PM")));

            verify(classScopeResolver).insert(notifId, List.of(classId),
                    EnumSet.of(TypeUser.STUDENT, TypeUser.REPRESENTATIVE), EnumSet.of(Shift.FULL_AM_PM));
        }

        @Test
        @DisplayName("filtro SHIFT com valor inválido deve lançar InvalidNotificationPayloadException")
        void filtroShiftInvalidoLancaExcecao() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            assertThatThrownBy(() ->
                    adapter.dispatch(notif,
                            List.of(scopeClass(UUID.randomUUID().toString())),
                            List.of(filterShift("INVALIDO")))
            )
                    .isInstanceOf(InvalidNotificationPayloadException.class)
                    .hasMessageContaining("INVALIDO");
        }

        @Test
        @DisplayName("sem filtros SHIFT deve repassar EnumSet vazio para os resolvers")
        void semFiltroShiftRepassaEnumSetVazio() {
            UUID notifId = UUID.randomUUID();
            UUID classId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeClass(classId.toString())),
                    List.of());

            verify(classScopeResolver).insert(notifId, List.of(classId),
                    EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
        }

        @Test
        @DisplayName("filtros SHIFT só afetam CLASS/COURSE — escopo USER não é filtrado por shift")
        void filtroShiftNaoAfetaEscopoUser() {
            UUID notifId = UUID.randomUUID();
            UUID userId  = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            // mesmo com SHIFT=FULL_AM_PM, o userDirectResolver deve receber o userId normalmente
            adapter.dispatch(notif,
                    List.of(scopeUser(userId.toString())),
                    List.of(filterShift("FULL_AM_PM")));

            verify(userDirectResolver).insert(notifId, Set.of(userId));
        }

        @Test
        @DisplayName("filtro SHIFT nao deve restringir escopo GLOBAL")
        void filtroShiftNaoAfetaEscopoGlobal() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            adapter.dispatch(notif,
                    List.of(scopeGlobal(null)),
                    List.of(filterShift("FULL_AM_PM")));

            verify(globalScopeResolver).insert(notifId, EnumSet.noneOf(TypeUser.class));
        }
    }

    @Nested
    @DisplayName("validação de UUID")
    class ValidacaoUuid {

        @Test
        @DisplayName("UUID inválido em escopo USER deve lançar InvalidNotificationPayloadException")
        void uuidInvalidoEmScopoUserLancaExcecao() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            assertThatThrownBy(() ->
                    adapter.dispatch(notif, List.of(scopeUser("nao-e-uuid")), List.of())
            )
                    .isInstanceOf(InvalidNotificationPayloadException.class)
                    .hasMessageContaining("nao-e-uuid");
        }

        @Test
        @DisplayName("UUID inválido em escopo CLASS deve lançar InvalidNotificationPayloadException")
        void uuidInvalidoEmScopoClassLancaExcecao() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            assertThatThrownBy(() ->
                    adapter.dispatch(notif, List.of(scopeClass("abc-123")), List.of())
            )
                    .isInstanceOf(InvalidNotificationPayloadException.class)
                    .hasMessageContaining("abc-123");
        }

        @Test
        @DisplayName("UUID inválido em escopo COURSE deve lançar InvalidNotificationPayloadException")
        void uuidInvalidoEmScopoCourse () {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            assertThatThrownBy(() ->
                    adapter.dispatch(notif, List.of(scopeCourse("xyz")), List.of())
            )
                    .isInstanceOf(InvalidNotificationPayloadException.class)
                    .hasMessageContaining("xyz");
        }

        @Test
        @DisplayName("UUID válido não deve lançar exceção")
        void uuidValidoNaoLancaExcecao() {
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);
            String validUuid = UUID.randomUUID().toString();

            // não deve lançar
            adapter.dispatch(notif, List.of(scopeUser(validUuid)), List.of());
        }
    }

    @Nested
    @DisplayName("todos os resolvers são sempre chamados")
    class TodosResolversSaoChamados {

        @Test
        @DisplayName("mesmo com lista de escopos vazia, os resolvers de escopo direto devem ser invocados com coleções vazias")
        void resolversSaoChamadosMesmoSemEscopos() {
            // o command já valida que scopes não pode ser vazio,
            // mas o adapter em si não impõe essa regra — testamos o comportamento isolado
            UUID notifId = UUID.randomUUID();
            NotificationEntity notif = notification(notifId);

            // passa lista vazia diretamente ao adapter (bypassando a validação do command)
            adapter.dispatch(notif, List.of(), List.of());

            verify(userDirectResolver).insert(eq(notifId), argThat(Set::isEmpty));
            verify(classScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
            verify(courseScopeResolver).insert(eq(notifId), argThat(List::isEmpty), any(), any());
            verifyNoInteractions(globalScopeResolver);
        }
    }
}
