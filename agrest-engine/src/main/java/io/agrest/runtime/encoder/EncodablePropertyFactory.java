package io.agrest.runtime.encoder;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.encoder.EncodableProperty;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.IdEncoder;
import io.agrest.encoder.ValueEncoders;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.ProcessingContext;
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
    public EncodableProperty getRelationshipProperty(
            ResourceEntity<?> entity,
            AgRelationship relationship,
            Encoder relatedEncoder,
            ProcessingContext<?> context) {

        NestedResourceEntity childEntity = entity.getChild(relationship.getName());
        PropertyReader reader = relationship.getResolver().reader(childEntity, context);

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

        Collection<AgIdPart> ids = entity.getAgEntity().getIdParts();

        // TODO: this is a hack - we are treating "id" as a "virtual" attribute, as there's generally no "id"
        //   property in AgEntity. See the same note in EntityPathCache

        switch (ids.size()) {
            case 0:
                return Optional.empty();
            case 1:
                AgIdPart idPart = entity.getAgEntity().getIdParts().iterator().next();
                EncodableProperty p1 = EncodableProperty
                        .property(entity.getAgEntity().getIdReader())
                        .encodedWith(new IdEncoder(getEncoder(idPart.getType())));
                return Optional.of(p1);

            default:

                // keeping attribute encoders in alphabetical order
                Map<String, Encoder> valueEncoders = new TreeMap<>();
                for (AgIdPart id : ids) {
                    valueEncoders.put(id.getName(), getEncoder(id.getType()));
                }

                EncodableProperty p2 = EncodableProperty
                        .property(entity.getAgEntity().getIdReader())
                        .encodedWith(new IdEncoder(valueEncoders));
                return Optional.of(p2);
        }
    }

    protected Encoder getEncoder(Class<?> type) {
        return valueEncoders.getEncoder(type);
    }
}
