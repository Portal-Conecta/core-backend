package com.portal.conecta.hub.module.room.application.use_case;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.domain.port.RoomRepository;
import com.portal.conecta.hub.module.room.presentation.dto.BulkRoomResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRoomsBulkUseCaseTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private GetRoomsBulkUseCase useCase;

    @Test
    @DisplayName("deve retornar bulk response separando encontrados e ausentes")
    void shouldReturnBulkResponseSeparatingFoundAndMissing() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        RoomEntity room = new RoomEntity(101, TypeRoom.CLASSROOM);
        ReflectionTestUtils.setField(room, "id", id1);

        when(roomRepository.findAllByIdInAndDeletedAtIsNull(List.of(id1, id2))).thenReturn(List.of(room));

        BulkRoomResponse result = useCase.execute(List.of(id1, id2));

        assertThat(result.items()).hasSize(1);
        assertThat(result.foundIds()).containsExactly(id1);
        assertThat(result.missingIds()).containsExactly(id2);

        verify(roomRepository).findAllByIdInAndDeletedAtIsNull(List.of(id1, id2));
    }

    @Test
    @DisplayName("deve ignorar ids duplicados na consulta e na resposta")
    void shouldIgnoreDuplicateIds() {
        UUID id1 = UUID.randomUUID();

        RoomEntity room = new RoomEntity(101, TypeRoom.CLASSROOM);
        ReflectionTestUtils.setField(room, "id", id1);

        when(roomRepository.findAllByIdInAndDeletedAtIsNull(List.of(id1))).thenReturn(List.of(room));

        BulkRoomResponse result = useCase.execute(List.of(id1, id1));

        assertThat(result.items()).hasSize(1);
        assertThat(result.foundIds()).containsExactly(id1);
        assertThat(result.missingIds()).isEmpty();

        verify(roomRepository).findAllByIdInAndDeletedAtIsNull(List.of(id1));
    }

    @Test
    @DisplayName("deve retornar lista vazia quando nenhum id for encontrado")
    void shouldReturnEmptyWhenNoIdsFound() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(roomRepository.findAllByIdInAndDeletedAtIsNull(List.of(id1, id2))).thenReturn(List.of());

        BulkRoomResponse result = useCase.execute(List.of(id1, id2));

        assertThat(result.items()).isEmpty();
        assertThat(result.foundIds()).isEmpty();
        assertThat(result.missingIds()).containsExactlyInAnyOrder(id1, id2);

        verify(roomRepository).findAllByIdInAndDeletedAtIsNull(List.of(id1, id2));
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando lista de ids for nula")
    void shouldThrowWhenIdsIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ids is required");

        verifyNoInteractions(roomRepository);
    }
}