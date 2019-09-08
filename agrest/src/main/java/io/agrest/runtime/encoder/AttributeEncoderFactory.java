package io.agrest.runtime.encoder;

import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.IdEncoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.property.IdReader;
import io.agrest.property.PropertyBuilder;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeEncoderFactory implements IAttributeEncoderFactory {

    private ValueEncoders valueEncoders;

    // these are explicit overrides for named attributes
    private Map<String, EntityProperty> attributePropertiesByPath;
    private Map<String, Optional<EntityProperty>> idPropertiesByEntity;

    public AttributeEncoderFactory(@Inject ValueEncoders valueEncoders) {
        this.valueEncoders = valueEncoders;
        this.attributePropertiesByPath = new ConcurrentHashMap<>();
        this.idPropertiesByEntity = new ConcurrentHashMap<>();
    }

    @Override
    public EntityProperty getAttributeProperty(AgEntity<?> entity, AgAttribute attribute) {
        String key = entity.getName() + "." + attribute.getName();
        return attributePropertiesByPath.computeIfAbsent(key, k -> buildAttributeProperty(attribute));
    }

    @Override
    public EntityProperty getRelationshipProperty(ResourceEntity<?> entity, AgRelationship relationship, Encoder encoder) {

        // Can't cache the reader, as both reader and encoder depend on request state
        PropertyReader reader = relationship.getPropertyReader(entity);

        // all relationships these days have a reader, but check just in case
        return reader != null
                ? PropertyBuilder.property(reader).encodedWith(encoder)
                : PropertyBuilder.property().encodedWith(encoder);
    }

    @Override
    public Optional<EntityProperty> getIdProperty(ResourceEntity<?> entity) {
        String key = entity.getAgEntity().getName();
        return idPropertiesByEntity.computeIfAbsent(key, k -> buildIdProperty(entity));
    }

    protected EntityProperty buildAttributeProperty(AgAttribute attribute) {
        Encoder encoder = getEncoder(attribute.getType());

        // all attributes these days have a reader, but check just in case
        return attribute.getPropertyReader() != null
                ? PropertyBuilder.property(attribute.getPropertyReader()).encodedWith(encoder)
                : PropertyBuilder.property().encodedWith(encoder);
    }

    protected Optional<EntityProperty> buildIdProperty(ResourceEntity<?> entity) {

        Collection<AgAttribute> ids = entity.getAgEntity().getIds();

        switch (ids.size()) {
            case 0:
                return Optional.empty();
            case 1:

                // TODO: abstraction leak... IdReader is not a property (it doesn't take property name to resolve a value),
                //  yet in EntityProperty it is treated as a property, so wrapping it in one

                IdReader ir1 = entity.getAgEntity().getIdReader();
                EntityProperty p1 = PropertyBuilder
                        .property((r, n) -> ir1.id(r))
                        .encodedWith(new IdEncoder(getEncoder(ids.iterator().next().getType())));
                return Optional.of(p1);
                
            default:

                // keeping attribute encoders in alphabetical order
                Map<String, Encoder> valueEncoders = new TreeMap<>();
                for (AgAttribute id : ids) {
                    valueEncoders.put(id.getName(), getEncoder(id.getType()));
                }

                // TODO: abstraction leak... IdReader is not a property (it doesn't take property name to resolve a value),
                //  yet in EntityProperty it is treated as a property, so wrapping it in one
                IdReader ir2 = entity.getAgEntity().getIdReader();
                EntityProperty p2 = PropertyBuilder
                        .property((r, n) -> ir2.id(r))
                        .encodedWith(new IdEncoder(valueEncoders));
                return Optional.of(p2);
        }
    }

    protected Encoder getEncoder(Class<?> type) {
        return valueEncoders.getEncoder(type);
    }
}
