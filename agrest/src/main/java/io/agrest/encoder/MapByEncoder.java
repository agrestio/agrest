package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;
import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.converter.StringConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IStringConverterFactory;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

public class MapByEncoder implements CollectionEncoder {

    private String mapByPath;
    private List<Function<Object, ?>> mapByReaders;
    private CollectionEncoder collectionEncoder;
    private boolean byId;
    private StringConverter fieldNameConverter;

    public MapByEncoder(
            String mapByPath,
            ResourceEntity<?> mapBy,
            CollectionEncoder collectionEncoder,
            IStringConverterFactory converterFactory,
            IAttributeEncoderFactory encoderFactory) {

        Objects.requireNonNull(mapBy, "Null mapBy");

        this.mapByPath = mapByPath;
        this.mapByReaders = new ArrayList<>();
        this.collectionEncoder = collectionEncoder;

        config(converterFactory, encoderFactory, mapBy);
    }

    private static Function<Object, ?> getPropertyReader(String propertyName, EntityProperty property) {
        return it -> property.read(it, propertyName);
    }

    @Override
    public boolean willEncode(String propertyName, Object object) {
        return true;
    }

    /**
     * @since 2.0
     */
    @Override
    public int visitEntities(Object object, EncoderVisitor visitor) {
        // a "flat" visit method that ignores mapping property
        return collectionEncoder.visitEntities(object, visitor);
    }

    private void config(
            IStringConverterFactory converterFactory,
            IAttributeEncoderFactory encoderFactory,
            ResourceEntity<?> mapBy) {

        if (mapBy.isIdIncluded()) {
            validateLeafMapBy(mapBy);
            byId = true;

            encoderFactory.getIdProperty(mapBy).ifPresent(p -> this.mapByReaders.add(getPropertyReader(null, p)));
            this.fieldNameConverter = converterFactory.getConverter(mapBy.getAgEntity());
            return;
        }

        if (!mapBy.getAttributes().isEmpty()) {

            validateLeafMapBy(mapBy);
            byId = false;

            Map.Entry<String, AgAttribute> attribute = mapBy.getAttributes().entrySet().iterator().next();
            mapByReaders.add(getPropertyReader(
                    attribute.getKey(),
                    encoderFactory.getAttributeProperty(mapBy.getAgEntity(), attribute.getValue())));

            this.fieldNameConverter = converterFactory.getConverter(mapBy.getAgEntity(), attribute.getKey());
            return;
        }

        if (!mapBy.getChildren().isEmpty()) {

            byId = false;

            Map.Entry<String, ResourceEntity<?>> child = mapBy.getChildren().entrySet().iterator().next();
            AgRelationship relationship = mapBy.getAgEntity().getRelationship(child.getKey());
            mapByReaders.add(getPropertyReader(
                    child.getKey(),
                    encoderFactory.getRelationshipProperty(mapBy, relationship, null)));

            ResourceEntity<?> childMapBy = mapBy.getChildren().get(child.getKey());
            config(converterFactory, encoderFactory, childMapBy);
            return;
        }

        // by default we are dealing with ID
        byId = true;
        encoderFactory.getIdProperty(mapBy).ifPresent(p -> mapByReaders.add(getPropertyReader(null, p)));
        fieldNameConverter = converterFactory.getConverter(mapBy.getAgEntity());
    }

    private void validateLeafMapBy(ResourceEntity<?> mapBy) {

        if (!mapBy.getChildren().isEmpty()) {

            StringBuilder message = new StringBuilder("'mapBy' path segment '")
                    .append(mapBy.getIncoming().getName())
                    .append("should not have children. Full 'mapBy' path: " + mapByPath);

            throw new AgException(Status.BAD_REQUEST, message.toString());
        }
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

        @SuppressWarnings({"rawtypes", "unchecked"})
        List<?> objects = (List) object;

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

        for (Function<Object, ?> reader : mapByReaders) {
            if (result == null) {
                break;
            }

            result = reader.apply(result);
        }

        return result;
    }

    private Map<String, List<Object>> mapBy(List<?> objects) {

        if (objects.isEmpty()) {
            return Collections.emptyMap();
        }

        // though the map is unsorted, it is still in predictable iteration
        // order...
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
                throw new AgException(Status.INTERNAL_SERVER_ERROR, "Null mapBy value for key '" + mapByPath + "'");
            }

            String keyString = fieldNameConverter.asString(key);

            List<Object> list = map.get(keyString);
            if (list == null) {
                list = new ArrayList<>();
                map.put(keyString, list);
            }

            list.add(o);
        }

        return map;
    }
}
