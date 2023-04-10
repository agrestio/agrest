package io.agrest.runtime.protocol;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.protocol.Exp;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;

import java.util.*;

public class ExpParser implements IExpParser {

    private static final String JSON_KEY_EXP = "exp";
    private static final String JSON_KEY_PARAMS = "params";

    private final IJacksonService jsonParser;

    public ExpParser(@Inject IJacksonService jsonParser) {
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
    public Exp fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Exp exp;

        if (value.startsWith("[")) {
            exp = fromJsonArray(jsonParser.parseJson(value));
        } else if (value.startsWith("{")) {
            exp = fromJsonObject(jsonParser.parseJson(value));
        } else {
            exp = Exp.from(value);
        }

        return exp;
    }

    /**
     * @since 2.13
     */
    @Override
    public Exp fromJson(JsonNode json) {

        if (json == null || json.isNull()) {
            return null;
        }

        if (json.isTextual()) {
            return Exp.from(json.asText());
        }

        if (json.isArray()) {
            return fromJsonArray(json);
        }

        if (json.isObject()) {
            return fromJsonObject(json);
        }

        // TODO: throw?
        return null;
    }

    private Exp fromJsonObject(JsonNode node) {
        // 'exp' key is required; 'params' key is optional
        JsonNode expNode = node.get(JSON_KEY_EXP);
        if (expNode == null) {
            throw AgException.badRequest("'exp' key is missing in 'exp' map");
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

            return Exp.from(expNode.asText()).withNamedParams(paramsMap);
        }

        return Exp.from(expNode.asText());
    }

    private Exp fromJsonArray(JsonNode array) {
        int len = array.size();

        if (len < 1) {
            throw AgException.badRequest("array 'exp' mast have at least one element");
        }

        String expString = array.get(0).asText();

        if (len < 2) {
            return Exp.from(expString);
        }

        Object[] params = new Object[len - 1];

        for (int i = 1; i < len; i++) {

            JsonNode paramNode = array.get(i);
            params[i - 1] = extractValue(paramNode);
        }

        return Exp.from(expString).withPositionalParams(params);
    }
}
