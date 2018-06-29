package com.nhl.link.rest.provider;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.query.CayenneExp;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Provider
public class CayenneExpProvider implements ParamConverterProvider {

    protected final IJacksonService jsonParser;


    public CayenneExpProvider(@Inject IJacksonService jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType != CayenneExp.class) ? null : new ParamConverter<T>() {
            private static final String EXP = "exp";
            private static final String PARAMS = "params";

            @Override
            public T fromString(final String param) {
                if (param == null || !param.startsWith("{")) {
                    return null;
                }
                try {

                    JsonNode map = jsonParser.parseJson(param);

                    // 'exp' key is required; 'params' key is optional
                    JsonNode expNode = map.get(EXP);
                    if (expNode == null) {
                        throw new LinkRestException(Response.Status.BAD_REQUEST, "'exp' key is missing in 'cayenneExp' map");
                    }

                    CayenneExp cayenneExp = new CayenneExp();
                    cayenneExp.setExp(expNode.asText());

                    JsonNode paramsNode = map.get(PARAMS);
                    if (paramsNode != null) {

                        Map<String, Object> paramsMap = new HashMap<>();

                        Iterator<String> it = paramsNode.fieldNames();
                        while (it.hasNext()) {
                            String key = it.next();
                            JsonNode valueNode = paramsNode.get(key);
                            Object value = extractValue(valueNode);
                            paramsMap.put(key, value);
                        }

                        cayenneExp.setParams(paramsMap);
                    }

                    return rawType.cast(cayenneExp);
                } catch (final Exception ex) {
                    throw new LinkRestException(Response.Status.BAD_REQUEST, "could not parse query parameter " + rawType.getName());
                }
            }

            @Override
            public String toString(final T value) throws IllegalArgumentException {
                if (value == null) {
                    return "";
                }
                return value.toString();
            }
        };
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
}
