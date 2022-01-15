package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;
import io.agrest.converter.valuejson.ValueJsonConverter;
import io.agrest.property.PropertyReader;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class MapByEncoder implements CollectionEncoder {

    private final String mapByPath;
    private final List<PropertyReader> mapByReaders;
    private final CollectionEncoder collectionEncoder;
    private final boolean byId;
    private final ValueJsonConverter fieldNameConverter;

    public MapByEncoder(
            String mapByPath,
            List<PropertyReader> mapByReaders,
            CollectionEncoder collectionEncoder,
            boolean byId,
            ValueJsonConverter fieldNameConverter) {

        this.mapByPath = mapByPath;
        this.mapByReaders = mapByReaders;
        this.collectionEncoder = collectionEncoder;
        this.byId = byId;
        this.fieldNameConverter = fieldNameConverter;
    }

    /**
     * @since 2.0
     */
    @Override
    public int visitEntities(Object object, EncoderVisitor visitor) {
        // a "flat" visit method that ignores mapping property
        return collectionEncoder.visitEntities(object, visitor);
    }

    @Override
    public int encodeAndGetTotal(String propertyName, Object object, JsonGenerator out) throws IOException {
        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }

        if (object == null) {
            out.writeNull();
            return 0;
        }

        List<?> objects = (List<?>) object;
        Map<String, List<Object>> map = mapBy(objects);

        out.writeStartObject();

        int total = 0;
        for (Entry<String, List<Object>> e : map.entrySet()) {
            out.writeFieldName(e.getKey());
            total += collectionEncoder.encodeAndGetTotal(null, e.getValue(), out);
        }

        out.writeEndObject();

        return total;
    }

    private Object mapByValue(Object object) {
        Object result = object;

        for (PropertyReader reader : mapByReaders) {
            if (result == null) {
                break;
            }

            result = reader.value(result);
        }

        return result;
    }

    private Map<String, List<Object>> mapBy(List<?> objects) {

        if (objects.isEmpty()) {
            return Collections.emptyMap();
        }

        // though the map is unsorted, it is still in predictable iteration order...
        Map<String, List<Object>> map = new LinkedHashMap<>();

        for (Object o : objects) {
            Object key = mapByValue(o);
            if (byId) {
                @SuppressWarnings("unchecked")
                Map<String, Object> id = (Map<String, Object>) key;
                key = id.entrySet().iterator().next().getValue();
            }

            // disallow nulls as JSON keys...
            // note that converter below will throw an NPE if we pass NULL
            // further down... the error here has more context.
            if (key == null) {
                throw AgException.internalServerError("Null mapBy value for key '%s'", mapByPath);
            }

            String keyString = fieldNameConverter.asString(key);
            map.computeIfAbsent(keyString, k -> new ArrayList<>()).add(o);
        }

        return map;
    }
}
