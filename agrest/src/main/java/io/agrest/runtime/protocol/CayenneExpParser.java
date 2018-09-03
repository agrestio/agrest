package io.agrest.runtime.protocol;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.LinkRestException;
import io.agrest.protocol.CayenneExp;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CayenneExpParser implements ICayenneExpParser {

    private static final String JSON_KEY_EXP = "exp";
    private static final String JSON_KEY_PARAMS = "params";

    private IJacksonService jsonParser;

    public CayenneExpParser(@Inject IJacksonService jsonParser) {
        this.jsonParser = jsonParser;
    }

    private static Object extractValue(JsonNode valueNode) {
        JsonToken type = valueNode.asToken();

        switch (type) {
            case VALUE_NUMBER_INT:
                return valueNode.asInt();
            case VALUE_NUMBER_FLOAT:
                return valueNode.asDouble();
            case VALUE_TRUE:
                return Boolean.TRUE;
            case VALUE_FALSE:
                return Boolean.FALSE;
            case VALUE_NULL:
                return null;
            case START_ARRAY:
                return extractArray(valueNode);
            default:
                // String parameters may need to be parsed further. Defer parsing
                // until it is placed in the context of an expression...
                return valueNode;
        }
    }

    private static List<Object> extractArray(JsonNode arrayNode) {
        List<Object> values = new ArrayList<>(arrayNode.size());

        for (JsonNode value : arrayNode) {
            values.add(extractValue(value));
        }

        return values;
    }

    /**
     * @since 2.13
     */
    @Override
    public CayenneExp fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        CayenneExp cayenneExp;

        if (value.startsWith("[")) {
            cayenneExp = fromJsonArray(jsonParser.parseJson(value));
        } else if (value.startsWith("{")) {
            cayenneExp = fromJsonObject(jsonParser.parseJson(value));
        } else {
            cayenneExp = new CayenneExp(value);
        }

        return cayenneExp;
    }

    /**
     * @since 2.13
     */
    @Override
    public CayenneExp fromJson(JsonNode json) {

        if (json == null || json.isNull()) {
            return null;
        }

        if(json.isTextual()) {
            return new CayenneExp(json.asText());
        }

        if(json.isArray()) {
            return fromJsonArray(json);
        }

        if(json.isObject()) {
            return fromJsonObject(json);
        }

        // TODO: throw?
        return null;
    }

    private CayenneExp fromJsonObject(JsonNode node) {
        // 'exp' key is required; 'params' key is optional
        JsonNode expNode = node.get(JSON_KEY_EXP);
        if (expNode == null) {
            throw new LinkRestException(Status.BAD_REQUEST, "'exp' key is missing in 'cayenneExp' map");
        }

        JsonNode paramsNode = node.get(JSON_KEY_PARAMS);
        if (paramsNode != null) {

            Map<String, Object> paramsMap = new HashMap<>();

            Iterator<String> it = paramsNode.fieldNames();
            while (it.hasNext()) {
                String key = it.next();
                JsonNode valueNode = paramsNode.get(key);
                Object val = extractValue(valueNode);
                paramsMap.put(key, val);
            }

            return new CayenneExp(expNode.asText(), paramsMap);
        }

        return new CayenneExp(expNode.asText());
    }

    private CayenneExp fromJsonArray(JsonNode array) {
        int len = array.size();

        if (len < 1) {
            throw new LinkRestException(Status.BAD_REQUEST, "array 'cayenneExp' mast have at least one element");
        }

        String expString = array.get(0).asText();

        if (len < 2) {
            return new CayenneExp(expString);
        }

        Object[] params = new Object[len - 1];

        for (int i = 1; i < len; i++) {

            JsonNode paramNode = array.get(i);
            params[i - 1] = extractValue(paramNode);
        }

        return new CayenneExp(expString, params);
    }
}
