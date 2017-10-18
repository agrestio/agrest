package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;

import javax.ws.rs.core.Response;

/**
 * @since 2.10
 */
public class EnumConverter<T extends Enum<T>> extends AbstractConverter<T> {

    private Class<T> enumType;

    public EnumConverter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    protected T valueNonNull(JsonNode node) {
        String value = node.asText();
        return value == null || value.length() == 0 ? null : fromString(node.asText());
    }

    protected T fromString(String string) {
        try {
            return Enum.valueOf(enumType, string);
        } catch (IllegalArgumentException e) {
            throw new LinkRestException(Response.Status.BAD_REQUEST,
                    "Invalid enum value: " + string);
        }
    }

    public Class<? extends Enum> getEnumType() {
        return enumType;
    }
}
