package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class EntityMetadataEncoder extends AbstractEncoder {

    private ResourceEntity<?> entity;
    private Map<String, PropertyMetadataEncoder> propertyMetadataEncoders;
    private Map<String, PropertyHelper> properties;

    public EntityMetadataEncoder(ResourceEntity<?> entity, Map<String, PropertyMetadataEncoder> propertyMetadataEncoders) {
        this.entity = entity;
        this.propertyMetadataEncoders = propertyMetadataEncoders;

        this.properties = new TreeMap<>();
        for (AgAttribute attribute : entity.getAttributes().values()) {
            properties.put(attribute.getName(), new AttributeProperty(attribute));
        }
        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            properties.put(child.getIncoming().getName(), new RelationshipProperty(child.getIncoming()));
        }
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        // sanity check
        if (!entity.equals(object)) {
            throw new IllegalArgumentException(
                    "Expected entity: " + entity.getName() + ", was object of class: " + object.getClass().getName()
            );
        }

        out.writeStartObject();

        out.writeStringField("name", entity.getName());

        out.writeArrayFieldStart("properties");
        for (Map.Entry<String, PropertyHelper> e : properties.entrySet()) {

            String typeName;
            if (byte[].class.equals(e.getValue().getType())) {
                typeName = "byte[]";
            } else {
                typeName = e.getValue().getType().getName();
            }
            Encoder encoder = propertyMetadataEncoders.get(typeName);
            if (encoder == null) {
                encoder = PropertyMetadataEncoder.encoder();
            }
            encoder.encode(null, e.getValue().getProperty(), out);
        }
        out.writeEndArray();

        out.writeEndObject();

        return true;
    }

    private static abstract class PropertyHelper {
        abstract Class<?> getType();
        abstract Object getProperty();
    }

    private static class AttributeProperty extends PropertyHelper {
        private AgAttribute attribute;

        AttributeProperty(AgAttribute attribute) {
            this.attribute = attribute;
        }

        @Override
        Class<?> getType() {
            return attribute.getType();
        }

        @Override
        Object getProperty() {
            return attribute;
        }
    }

    private static class RelationshipProperty extends PropertyHelper {
        private AgRelationship relationship;

        RelationshipProperty(AgRelationship relationship) {
            this.relationship = relationship;
        }

        @Override
        Class<?> getType() {
            return relationship.getTargetEntity().getType();
        }

        @Override
        Object getProperty() {
            return relationship;
        }
    }

}
