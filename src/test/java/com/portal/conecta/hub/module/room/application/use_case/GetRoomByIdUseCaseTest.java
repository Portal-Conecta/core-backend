package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRoomByIdUseCaseTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private GetRoomByIdUseCase useCase;

    @Test
    @DisplayName("deve retornar sala ativa quando encontrada")
    void shouldReturnRoomWhenFound() {
        UUID roomId = UUID.randomUUID();
        RoomEntity room = new RoomEntity(101, TypeRoom.CLASSROOM);

        when(roomRepository.findByIdAndDeletedAtIsNull(roomId)).thenReturn(Optional.of(room));

        RoomEntity result = useCase.execute(roomId);

        assertThat(result).isEqualTo(room);
        verify(roomRepository).findByIdAndDeletedAtIsNull(roomId);
    }

    @Test
    @DisplayName("deve lançar RoomNotFoundException quando sala não existe")
    void shouldThrowWhenRoomNotFound() {
        UUID roomId = UUID.randomUUID();

        when(roomRepository.findByIdAndDeletedAtIsNull(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(roomId))
                .isInstanceOf(RoomNotFoundException.class);

        verify(roomRepository).findByIdAndDeletedAtIsNull(roomId);
    }

    @Test
    @DisplayName("deve lançar RoomNotFoundException quando sala está removida logicamente")
    void shouldThrowWhenRoomIsDeleted() {
        UUID roomId = UUID.randomUUID();

        when(roomRepository.findByIdAndDeletedAtIsNull(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(roomId))
                .isInstanceOf(RoomNotFoundException.class);

        verify(roomRepository).findByIdAndDeletedAtIsNull(roomId);
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando id é nulo")
    void shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(roomRepository);
    }
}