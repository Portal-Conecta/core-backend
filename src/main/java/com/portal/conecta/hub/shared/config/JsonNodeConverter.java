package com.portal.conecta.hub.shared.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter JPA para persistir {@link tools.jackson.databind.JsonNode} como {@code TEXT} no banco.
 *
 * <p>Serializa o nó JSON para {@code String} ao gravar e desserializa ao ler.
 * Retorna {@code null} silenciosamente em caso de falha de parsing ou valor ausente,
 * sem propagar exceção — revisar se o comportamento silencioso é adequado para o contexto de uso.
 */
@Converter
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private static final JsonMapper MAPPER = new JsonMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return MAPPER.readTree(dbData);
        } catch (Exception e) {
            return null;
        }
    }
}