package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.EntityProperty;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @since 3.4
 */
public class EntityNoIdEncoder extends AbstractEncoder {

    private Map<String, EntityProperty> relationshipEncoders;
    private Map<String, EntityProperty> combinedEncoders;

    public EntityNoIdEncoder(
            Map<String, EntityProperty> attributeEncoders,
            Map<String, EntityProperty> relationshipEncoders) {

        // tracking relationship encoders separately for the sake of the visitors
        this.relationshipEncoders = relationshipEncoders;

        this.combinedEncoders = new TreeMap<>();
        combinedEncoders.putAll(attributeEncoders);
        combinedEncoders.putAll(relationshipEncoders);
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {

        out.writeStartObject();
        encodeProperties(object, out);
        out.writeEndObject();
        return true;
    }

    protected void encodeProperties(Object object, JsonGenerator out) throws IOException {

        for (Map.Entry<String, EntityProperty> e : combinedEncoders.entrySet()) {
            EntityProperty p = e.getValue();
            String propertyName = e.getKey();
            Object v = object == null ? null : p.getReader().value(object);
            p.getEncoder().encode(propertyName, v, out);
        }
    }

    @Override
    public int visitEntities(Object object, EncoderVisitor visitor) {

        if (object == null || !willEncode(null, object)) {
            return VISIT_CONTINUE;
        }

        int bitmask = visitor.visit(object);

        if ((bitmask & VISIT_SKIP_ALL) != 0) {
            return VISIT_SKIP_ALL;
        }

        if ((bitmask & VISIT_SKIP_CHILDREN) == 0) {

            for (Map.Entry<String, EntityProperty> e : relationshipEncoders.entrySet()) {

                visitor.push(e.getKey());

                int propBitmask = e.getValue().visit(object, visitor);

                if ((propBitmask & VISIT_SKIP_ALL) != 0) {
                    return VISIT_SKIP_ALL;
                }

                visitor.pop();
            }

        }

        return VISIT_CONTINUE;
    }
}
