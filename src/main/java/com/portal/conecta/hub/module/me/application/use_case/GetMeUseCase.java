package com.portal.conecta.hub.module.me.application.use_case;

import com.portal.conecta.hub.module.me.presentation.dto.MyProfileResponse;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Caso de uso responsável por recuperar os dados de perfil do usuário autenticado.
 * <p>
 * Segue a diretriz do módulo de atuar como uma fachada de leitura, utilizando o contexto
 * de segurança (Token JWT) para identificar o usuário. Isso garante que o cliente acesse
 * apenas as suas próprias informações básicas, sem precisar informar o ID na requisição.
 * </p>
 */
@Service
public class GetMeUseCase {

    private final RequestContextProvider requestContextProvider;
    private final UserRepository userRepository;

    public GetMeUseCase(RequestContextProvider requestContextProvider, UserRepository userRepository) {
        this.requestContextProvider = requestContextProvider;
        this.userRepository = userRepository;
    }

    /**
     * Executa a consulta do perfil com base no identificador extraído do contexto da requisição.
     * <p>
     * Além de buscar o registro, garante que os dados retornados pertençam a um usuário
     * que esteja explicitamente ativo e não tenha sofrido exclusão lógica (soft delete).
     * </p>
     *
     * @return MyProfileResponse DTO contendo os dados estruturados do perfil do usuário logado.
     * @throws UserNotFoundException Se o ID mapeado no contexto não for localizado na base,
     * ou se o usuário estiver inativo/excluído.
     */
    public MyProfileResponse execute() {
        RequestContext context = requestContextProvider.getRequestContext();

        return userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(context.userId())
                .map(MyProfileResponse::from)
                .orElseThrow(UserNotFoundException::new);
    }
}