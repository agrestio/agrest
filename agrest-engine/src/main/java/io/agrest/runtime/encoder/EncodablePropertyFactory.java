package io.agrest.runtime.encoder;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.encoder.EncodableProperty;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.IdEncoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;
import io.agrest.property.IdReader;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class EncodablePropertyFactory implements IEncodablePropertyFactory {

    private final ValueEncoders valueEncoders;
    private final Map<String, Optional<EncodableProperty>> idPropertiesByEntity;

    public EncodablePropertyFactory(@Inject ValueEncoders valueEncoders) {
        this.valueEncoders = valueEncoders;
        this.idPropertiesByEntity = new ConcurrentHashMap<>();
    }

    @Override
    public EncodableProperty getAttributeProperty(ResourceEntity<?> entity, AgAttribute attribute) {
        return EncodableProperty
                .property(attribute.getPropertyReader())
                .encodedWith(getEncoder(attribute.getType()));
    }

    @Override
    public EncodableProperty getRelationshipProperty(ResourceEntity<?> entity, AgRelationship relationship, Encoder relatedEncoder) {

        NestedResourceEntity childEntity = entity.getChild(relationship.getName());
        PropertyReader reader = relationship.getResolver().reader(childEntity);

        return EncodableProperty
                .property(reader)
                .encodedWith(relatedEncoder);
    }

    @Override
    public Optional<EncodableProperty> getIdProperty(ResourceEntity<?> entity) {
        // id properties are not (yet) overlaid and hence can be cached
        return idPropertiesByEntity
                .computeIfAbsent(entity.getName(), k -> buildIdProperty(entity));
    }

    protected Optional<EncodableProperty> buildIdProperty(ResourceEntity<?> entity) {

        Collection<AgAttribute> ids = entity.getAgEntity().getIds();

        switch (ids.size()) {
            case 0:
                return Optional.empty();
            case 1:

                IdReader ir1 = entity.getAgEntity().getIdReader();
                EncodableProperty p1 = EncodableProperty
                        .property(ir1::id)
                        .encodedWith(new IdEncoder(getEncoder(ids.iterator().next().getType())));
                return Optional.of(p1);

            default:

                // keeping attribute encoders in alphabetical order
                Map<String, Encoder> valueEncoders = new TreeMap<>();
                for (AgAttribute id : ids) {
                    valueEncoders.put(id.getName(), getEncoder(id.getType()));
                }

                IdReader ir2 = entity.getAgEntity().getIdReader();
                EncodableProperty p2 = EncodableProperty
                        .property(ir2::id)
                        .encodedWith(new IdEncoder(valueEncoders));
                return Optional.of(p2);
        }
    }

    protected Encoder getEncoder(Class<?> type) {
        return valueEncoders.getEncoder(type);
    }
}
