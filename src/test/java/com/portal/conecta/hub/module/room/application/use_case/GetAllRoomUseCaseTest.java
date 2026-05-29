package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllRoomUseCaseTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private GetAllRoomUseCase useCase;

    @Test
    @DisplayName("deve retornar lista de salas ativas")
    void shouldReturnActiveRooms() {
        RoomEntity room1 = new RoomEntity(101, TypeRoom.CLASSROOM);
        RoomEntity room2 = new RoomEntity(202, TypeRoom.LABORATORY);

        when(roomRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(room1, room2));

        List<RoomEntity> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result).contains(room1, room2);
        verify(roomRepository).findAllByDeletedAtIsNull();
    }

    @Test
    @DisplayName("deve retornar lista vazia quando não há salas ativas")
    void shouldReturnEmptyListWhenNoActiveRooms() {
        when(roomRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());

        List<RoomEntity> result = useCase.execute();

        assertThat(result).isEmpty();
        verify(roomRepository).findAllByDeletedAtIsNull();
    }
}