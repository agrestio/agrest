package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

/**
 * @since 2.13
 */
public class SizeParser implements ISizeParser {

    @Override
    public Integer startFromJson(JsonNode json) {

        if (json != null) {
            if (!json.isNumber()) {
                throw AgException.badRequest("Expected 'int' as 'start' value, got: %s", json);
            }

            return json.asInt();
        }

        return null;
    }

    @Override
    public Integer limitFromJson(JsonNode json) {

        if (json != null) {
            if (!json.isNumber()) {
                throw AgException.badRequest("Expected 'int' as 'limit' value, got: %s", json);
            }

            return json.asInt();
        }

        return null;
    }
}
