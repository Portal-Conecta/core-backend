package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.application.command.CreateRoomCommand;
import com.portal.conecta.hub.module.room.domain.exception.RoomNumberAlreadyInUseException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.room.domain.validator.RoomPermissionValidator;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException; // <-- NOVA IMPORTAÇÃO
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

/**
 * Caso de uso responsável por orquestrar a criação e o cadastro de uma nova sala física.
 */
@Component
@Slf4j
public class CreateRoomUseCase {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;

    public CreateRoomUseCase(RoomRepository roomRepository, UserRepository userRepository, RoomPermissionValidator permissionValidator, RequestContextProvider contextProvider) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    /**
     * Valida os requisitos de negócio e persiste uma nova sala no sistema.
     * O processo garante a validação do perfil do solicitante, a unicidade global do número da sala
     * (incluindo salas removidas) e vincula o usuário autenticado como o criador oficial do registro.
     *
     * @param command Dados estruturados para a criação da sala.
     * @return A entidade RoomEntity criada e persistida.
     * @throws RoomPermissionDeniedException se o tipo do usuário solicitante não possuir privilégios de criação.
     * @throws RoomNumberAlreadyInUseException se o número informado já estiver cadastrado no banco.
     * @throws UserNotFoundException se a entidade do usuário autenticado não for localizada na base de dados.
     */
    @Transactional
    public RoomEntity execute(CreateRoomCommand command) {
        RequestContext context = contextProvider.getRequestContext();

        if (!permissionValidator.canCreate(context.userType())) throw new RoomPermissionDeniedException();

        if (roomRepository.existsByNumber(command.number())) {
            throw new RoomNumberAlreadyInUseException(command.number());
        }


        UserEntity creator = userRepository.findById(context.userId())
                .orElseThrow(() -> new UserNotFoundException("Usuário autenticado não encontrado."));

        RoomEntity room = RoomEntity.create(command.number(), command.typeRoom(), creator);
        RoomEntity saved = roomRepository.save(room);

        log.info("Sala criada com sucesso. roomId={}, roomNumber={}, roomType={}, requesterUserId={}",
                saved.getId(), saved.getNumber(), saved.getTypeRoom(), context.userId());

        return saved;
    }
}