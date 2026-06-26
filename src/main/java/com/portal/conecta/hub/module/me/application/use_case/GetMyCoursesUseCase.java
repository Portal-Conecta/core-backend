package com.portal.conecta.hub.module.me.application.use_case;

import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.me.infrastructure.projection.UserCourseClassProjection;
import com.portal.conecta.hub.module.me.presentation.dto.MyClassResponse;
import com.portal.conecta.hub.module.me.presentation.dto.MyCourseResponse;
import com.portal.conecta.hub.module.me.presentation.dto.MyListCourseResponse;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Caso de Uso responsável por consolidar e listar os cursos e turmas associados ao usuário logado.
 * <p>
 * Orquestra a extração do identificador do usuário a partir do provedor de contexto da requisição,
 * aciona a camada de persistência através de projeções de leitura otimizadas e realiza o agrupamento
 * lógico dos dados estruturando a resposta final.
 * </p>
 */
@Component
public class GetMyCoursesUseCase {

    private final ClassMembershipRepository classMembershipRepository;
    private final RequestContextProvider requestContext;

    public GetMyCoursesUseCase(ClassMembershipRepository classMembershipRepository, RequestContextProvider requestContext) {
        this.classMembershipRepository = classMembershipRepository;
        this.requestContext = requestContext;
    }

    /**
     * Executa a consulta de relacionamentos acadêmicos com base no usuário autenticado.
     * <p>
     * O método recupera o identificador do usuário a partir do contexto de segurança,
     * busca as linhas da projeção pelo repositório de matrículas e monta um mapa
     * ordenado para agrupar as turmas dentro de seus respectivos cursos de forma hierárquica.
     * </p>
     * <p>
     * <b>Comportamento de resposta:</b> Se o usuário não possuir nenhum vínculo ativo ou se
     * a consulta não retornar registros, o método retorna um DTO inicializado com uma
     * lista de cursos vazia, sem lançar exceções de ausência de recurso.
     * </p>
     *
     * @return MyListCourseResponse DTO estruturado contendo a listagem hierárquica de cursos e turmas vinculadas.
     */
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
