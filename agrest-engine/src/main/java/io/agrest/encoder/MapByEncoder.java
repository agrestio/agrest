package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;
import io.agrest.converter.valuestring.ValueStringConverter;
import io.agrest.reader.DataReader;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class MapByEncoder extends AbstractEncoder {

    private final List<DataReader> mapByReaders;
    private final Encoder collectionEncoder;
    private final boolean byId;
    private final ValueStringConverter fieldNameConverter;

    public MapByEncoder(
            List<DataReader> mapByReaders,
            Encoder collectionEncoder,
            boolean byId,
            ValueStringConverter fieldNameConverter) {

        this.mapByReaders = mapByReaders;
        this.collectionEncoder = collectionEncoder;
        this.byId = byId;
        this.fieldNameConverter = fieldNameConverter;
    }

    @Override
    protected void encodeNonNullObject(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
        List<?> objects = (List<?>) object;
        Map<String, List<Object>> map = mapBy(objects);

        out.writeStartObject();

        for (Entry<String, List<Object>> e : map.entrySet()) {
            out.writeFieldName(e.getKey());
            collectionEncoder.encode(null, e.getValue(), skipNullProperties, out);
        }

        out.writeEndObject();
    }

    private Object mapByValue(Object object) {
        Object result = object;

        for (DataReader reader : mapByReaders) {
            if (result == null) {
                break;
            }

            result = reader.read(result);
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
            // note that converter below will throw an NPE if we pass "null" further down. The error here has more context.
            if (key == null) {
                throw AgException.internalServerError("Null mapBy value for object '%s'", o);
            }

            String keyString = fieldNameConverter.asString(key);
            map.computeIfAbsent(keyString, k -> new ArrayList<>()).add(o);
        }

        return map;
    }
}
