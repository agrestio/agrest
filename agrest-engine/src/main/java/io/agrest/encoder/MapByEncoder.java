package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.encoder.converter.StringConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.encoder.IEncodablePropertyFactory;
import io.agrest.runtime.encoder.IStringConverterFactory;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class MapByEncoder implements CollectionEncoder {

    private final String mapByPath;
    private final List<PropertyReader> mapByReaders;
    private final CollectionEncoder collectionEncoder;
    private boolean byId;
    private StringConverter fieldNameConverter;

    public MapByEncoder(
            String mapByPath,
            ResourceEntity<?> mapBy,
            CollectionEncoder collectionEncoder,
            IStringConverterFactory converterFactory,
            IEncodablePropertyFactory encodablePropertyFactory) {

        Objects.requireNonNull(mapBy, "Null mapBy");

        this.mapByPath = mapByPath;
        this.mapByReaders = new ArrayList<>();
        this.collectionEncoder = collectionEncoder;

        config(converterFactory, encodablePropertyFactory, mapBy);
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
            IEncodablePropertyFactory encodablePropertyFactory,
            ResourceEntity<?> mapBy) {

        if (mapBy.isIdIncluded()) {
            validateLeafMapBy(mapBy);
            byId = true;

            encodablePropertyFactory.getIdProperty(mapBy).ifPresent(p -> this.mapByReaders.add(p.getReader()));
            this.fieldNameConverter = converterFactory.getConverter(mapBy.getAgEntity());
            return;
        }

        if (!mapBy.getAttributes().isEmpty()) {

            validateLeafMapBy(mapBy);
            byId = false;

            Map.Entry<String, AgAttribute> attribute = mapBy.getAttributes().entrySet().iterator().next();
            this.mapByReaders.add(encodablePropertyFactory.getAttributeProperty(mapBy, attribute.getValue()).getReader());
            this.fieldNameConverter = converterFactory.getConverter(mapBy.getAgEntity(), attribute.getKey());
            return;
        }

        if (!mapBy.getChildren().isEmpty()) {

            byId = false;

            Map.Entry<String, NestedResourceEntity<?>> child = mapBy.getChildren().entrySet().iterator().next();

            // TODO: to account for overlaid relationships (and avoid NPEs), we should not access agEntity...
            //  instead should look for incoming rel of a child ResourceEntity.. Is is present in MapBy case?
            AgRelationship relationship = mapBy.getChild(child.getKey()).getIncoming();
            this.mapByReaders.add(encodablePropertyFactory.getRelationshipProperty(mapBy, relationship, null).getReader());

            ResourceEntity<?> childMapBy = mapBy.getChildren().get(child.getKey());
            config(converterFactory, encodablePropertyFactory, childMapBy);
            return;
        }

        // by default we are dealing with ID
        byId = true;
        encodablePropertyFactory.getIdProperty(mapBy).ifPresent(p -> mapByReaders.add(p.getReader()));
        fieldNameConverter = converterFactory.getConverter(mapBy.getAgEntity());
    }

    private void validateLeafMapBy(ResourceEntity<?> mapBy) {

        if (!mapBy.getChildren().isEmpty()) {

            String pathSegment = (mapBy instanceof NestedResourceEntity)
                    ? ((NestedResourceEntity<?>) mapBy).getIncoming().getName()
                    : "";

            throw new AgException(Status.BAD_REQUEST, "'mapBy' path segment '" + pathSegment +
                    "' should not have children. Full 'mapBy' path: " + mapByPath);
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
                throw new AgException(Status.INTERNAL_SERVER_ERROR, "Null mapBy value for key '" + mapByPath + "'");
            }

            String keyString = fieldNameConverter.asString(key);
            map.computeIfAbsent(keyString, k -> new ArrayList<>()).add(o);
        }

        return map;
    }
}
