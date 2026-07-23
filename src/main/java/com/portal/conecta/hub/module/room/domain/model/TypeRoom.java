package com.portal.conecta.hub.module.room.domain.model;

/**
 * Representa as categorias de salas físicas disponíveis no Hub.
 */
public enum TypeRoom {
    CLASSROOM,
    ELECTROTECHNICS_LABORATORY,
    ELECTRONICS_LABORATORY,
    COMPUTER_LABORATORY,
    CNC_SIMULATION;

    /**
     * Converte a constante do Enum para o formato minúsculo trafegado nos contratos de API.
     */
	public String toApiValue() {
		return name().toLowerCase();
	}

    /**
     * Interpreta um valor recebido da API, convertendo para a respectiva constante do domínio.
     * @param value O tipo de sala em formato string.
     * @return A constante correspondente ou null caso o valor seja inválido/vazio.
     */
	public static TypeRoom fromApiValue(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return TypeRoom.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
