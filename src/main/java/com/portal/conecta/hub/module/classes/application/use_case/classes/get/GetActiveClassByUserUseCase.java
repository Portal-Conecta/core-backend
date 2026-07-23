package com.portal.conecta.hub.module.classes.application.use_case.classes.get;

import java.util.List;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retorna os vínculos ativos de turma de um usuário específico.
 *
 * <p>Usuários ativos, desativados e pendentes de ativação são considerados válidos.
 * Turmas excluídas logicamente não são incluídas no resultado.</p>
 */
@Component
@Transactional
public class GetActiveClassByUserUseCase {

    private final UserRepository userRepository;
    private final ClassMembershipRepository classMembershipRepository;

    public GetActiveClassByUserUseCase(
            UserRepository userRepository,
            ClassMembershipRepository classMembershipRepository
    ) {
        this.userRepository = userRepository;
        this.classMembershipRepository = classMembershipRepository;
    }

    /**
     * Executa a consulta dos vínculos ativos do usuário.
     *
     * @param command identificador do usuário a ser consultado.
     * @return lista de vínculos ativos do usuário; pode ser vazia se não houver turmas.
     * @throws UserNotFoundException se o usuário não existir ou estiver removido.
     */
    public List<ClassMembershipEntity> execute(GetActiveClassByUserCommand command) {
        if (userRepository.findByIdAndAccountStatusNot(command.userId(), AccountStatus.PENDING_DELETION).isEmpty()) {
            throw new UserNotFoundException();
        }

        return classMembershipRepository.findActiveByUserId(command.userId());
    }
}
