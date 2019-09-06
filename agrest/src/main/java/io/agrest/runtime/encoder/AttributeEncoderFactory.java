package io.agrest.runtime.encoder;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.IdEncoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentRelationship;
import io.agrest.meta.AgRelationship;
import io.agrest.property.BeanPropertyReader;
import io.agrest.property.IdPropertyReader;
import io.agrest.property.PropertyBuilder;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeEncoderFactory implements IAttributeEncoderFactory {

    private ValueEncoders valueEncoders;

    // these are explicit overrides for named attributes
    private Map<String, EntityProperty> attributePropertiesByPath;
    private Map<String, Optional<EntityProperty>> idPropertiesByEntity;
    private Map<AgEntity<?>, IdPropertyReader> idPropertyReaders;

    public AttributeEncoderFactory(@Inject ValueEncoders valueEncoders) {
        this.valueEncoders = valueEncoders;
        this.attributePropertiesByPath = new ConcurrentHashMap<>();
        this.idPropertiesByEntity = new ConcurrentHashMap<>();
        this.idPropertyReaders = new ConcurrentHashMap<>();
    }

    @Override
    public EntityProperty getAttributeProperty(AgEntity<?> entity, AgAttribute attribute) {
        String key = entity.getName() + "." + attribute.getName();
        return attributePropertiesByPath.computeIfAbsent(key, k -> buildAttributeProperty(attribute));
    }

    @Override
    public EntityProperty getRelationshipProperty(ResourceEntity<?> entity, AgRelationship relationship, Encoder encoder) {

        // TODO: can't cache, as target encoder is dynamic...
        return buildRelationshipProperty(entity, relationship, encoder);
    }

    @Override
    public Optional<EntityProperty> getIdProperty(ResourceEntity<?> entity) {
        String key = entity.getAgEntity().getName();
        return idPropertiesByEntity.computeIfAbsent(key, k -> buildIdProperty(entity));
    }

    protected EntityProperty buildRelationshipProperty(
            ResourceEntity<?> entity,
            AgRelationship relationship,
            Encoder encoder) {

        // for now only "overlay" relationships have non-null readers
        if (relationship.getPropertyReader() != null) {
            return PropertyBuilder.property(relationship.getPropertyReader()).encodedWith(encoder);
        }

        boolean persistent = relationship instanceof AgPersistentRelationship;
        if (persistent && DataObject.class.isAssignableFrom(entity.getType())) {

            PropertyReader reader = (root, name) -> {

                AgObjectId id = readObjectId(entity.getAgEntity(), (DataObject) root);
                Object result = entity.getChild(name).getResult(id);

                return result == null && relationship.isToMany()
                        ? Collections.emptyList()
                        : result;
            };

            return PropertyBuilder.property(reader).encodedWith(encoder);
        }

        return PropertyBuilder.property().encodedWith(encoder);
    }

    protected EntityProperty buildAttributeProperty(AgAttribute attribute) {
        Encoder encoder = getEncoder(attribute.getType());
        return attribute.getPropertyReader() != null
                ? PropertyBuilder.property(attribute.getPropertyReader()).encodedWith(encoder)
                : PropertyBuilder.property().encodedWith(encoder);
    }

    protected Optional<EntityProperty> buildIdProperty(ResourceEntity<?> entity) {

        Collection<AgAttribute> ids = entity.getAgEntity().getIds();

        // TODO: dirty - direct Cayenne dependency
        // TODO: consider unifying id readers between POJO and entities

        if (DataObject.class.isAssignableFrom(entity.getType())) {

            // Cayenne object - PK is an ObjectId (even if it is also a meaningful object property)
            switch (ids.size()) {
                case 0:
                    return Optional.empty();
                case 1:
                    EntityProperty p1 = PropertyBuilder
                            .property(getOrCreateIdReader(entity.getAgEntity()))
                            .encodedWith(new IdEncoder(getEncoder(ids.iterator().next().getType())));
                    return Optional.of(p1);
                default:

                    // keeping attribute encoders in alphabetical order
                    Map<String, Encoder> valueEncoders = new TreeMap<>();
                    for (AgAttribute id : ids) {
                        valueEncoders.put(id.getName(), getEncoder(id.getType()));
                    }

                    EntityProperty p2 = PropertyBuilder
                            .property(getOrCreateIdReader(entity.getAgEntity()))
                            .encodedWith(new IdEncoder(valueEncoders));
                    return Optional.of(p2);
            }

        } else {

            // POJO - PK is an object property

            if (ids.isEmpty()) {
                return Optional.empty();
            }

            // TODO: multi-attribute ID?

            AgAttribute id = ids.iterator().next();
            return Optional.of(PropertyBuilder.property(BeanPropertyReader.reader(id.getName())));
        }
    }


    private AgObjectId readObjectId(AgEntity<?> entity, DataObject object) {

        Map<String, Object> idMap = (Map<String, Object>) getOrCreateIdReader(entity).value(object, null);

        if (idMap.size() == 1) {
            return new SimpleObjectId(idMap.values().iterator().next());
        } else if (idMap.size() > 1) {
            return new CompoundObjectId(idMap);
        }

        throw new RuntimeException("ID is empty for entity '" + entity.getName() + "'");
    }

    private IdPropertyReader getOrCreateIdReader(AgEntity<?> entity) {
        return idPropertyReaders.computeIfAbsent(entity, e -> new IdPropertyReader(e));
    }

    protected Encoder getEncoder(Class<?> type) {
        return valueEncoders.getEncoder(type);
    }
}
