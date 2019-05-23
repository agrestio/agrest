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
import io.agrest.meta.AgPersistentAttribute;
import io.agrest.meta.AgPersistentEntity;
import io.agrest.meta.AgPersistentRelationship;
import io.agrest.meta.AgRelationship;
import io.agrest.property.BeanPropertyReader;
import io.agrest.property.IdPropertyReader;
import io.agrest.property.PropertyBuilder;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.DataObject;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeEncoderFactory implements IAttributeEncoderFactory {

    static final Class<?> UTIL_DATE = Date.class;
    static final Class<?> SQL_DATE = java.sql.Date.class;
    static final Class<?> SQL_TIME = Time.class;
    static final Class<?> SQL_TIMESTAMP = Timestamp.class;
    static final Class<?> LOCAL_DATE = LocalDate.class;
    static final Class<?> LOCAL_TIME = LocalTime.class;
    static final Class<?> LOCAL_DATETIME = LocalDateTime.class;
    static final Class<?> OFFSET_DATETIME = OffsetDateTime.class;

    private Map<Class<?>, Encoder> encodersByJavaType;
    private Encoder defaultEncoder;

    // these are explicit overrides for named attributes
    private Map<String, EntityProperty> attributePropertiesByPath;
    private Map<String, EntityProperty> idPropertiesByEntity;
    private Map<AgEntity<?>, IdPropertyReader> idPropertyReaders;

    public AttributeEncoderFactory(Map<Class<?>, Encoder> knownEncoders,
                                   Encoder defaultEncoder) {
        // creating a concurrent copy of the provided map - we'll be expanding it dynamically.
        this.encodersByJavaType = new ConcurrentHashMap<>(knownEncoders);
        this.defaultEncoder = defaultEncoder;

        this.attributePropertiesByPath = new ConcurrentHashMap<>();
        this.idPropertiesByEntity = new ConcurrentHashMap<>();
        this.idPropertyReaders = new ConcurrentHashMap<>();
    }

    @Override
    public EntityProperty getAttributeProperty(AgEntity<?> entity, AgAttribute attribute) {
        String key = entity.getName() + "." + attribute.getName();
        return attributePropertiesByPath.computeIfAbsent(key, k -> buildAttributeProperty(entity, attribute));
    }

    @Override
    public EntityProperty getRelationshipProperty(ResourceEntity<?> entity, AgRelationship relationship, Encoder encoder) {

        // TODO: can't cache, as target encoder is dynamic...
        return buildRelationshipProperty(entity, relationship, encoder);
    }

    @Override
    public EntityProperty getIdProperty(ResourceEntity<?> entity) {

        String key = entity.getAgEntity().getName();

        EntityProperty property = idPropertiesByEntity.get(key);
        if (property == null) {
            property = buildIdProperty(entity);
            idPropertiesByEntity.put(key, property);
        }

        return property;
    }

    protected EntityProperty buildRelationshipProperty(ResourceEntity<?> entity, AgRelationship relationship, Encoder encoder) {

        // for now only "overlay" relationships have non-null readers
        if (relationship.getPropertyReader() != null) {
            return PropertyBuilder.property(relationship.getPropertyReader());
        }

        boolean persistent = relationship instanceof AgPersistentRelationship;
        if (persistent && DataObject.class.isAssignableFrom(entity.getType())) {

            PropertyReader reader = (root, name) -> {

                AgObjectId id = readObjectId(entity.getAgEntity(), (DataObject) root);

                ResourceEntity childEntity = entity.getChildren().get(name);
                Object result = childEntity.getResult(id);

                return result == null && relationship.isToMany()
                        ? Collections.emptyList()
                        : result;
            };

            return PropertyBuilder.property(reader).encodedWith(encoder);
        }

        return PropertyBuilder.property().encodedWith(encoder);
    }

    protected EntityProperty buildAttributeProperty(AgEntity<?> entity, AgAttribute attribute) {
        boolean persistent = attribute instanceof AgPersistentAttribute;
        Encoder encoder = buildEncoder(attribute);
        return getProperty(entity, attribute.getPropertyReader(), persistent, encoder);
    }

    private EntityProperty getProperty(AgEntity<?> entity, PropertyReader reader, boolean persistent, Encoder encoder) {
        if (persistent && DataObject.class.isAssignableFrom(entity.getType())) {
            return PropertyBuilder.dataObjectProperty().encodedWith(encoder);
        } else if (reader != null) {
            return PropertyBuilder.property(reader);
        } else {
            return PropertyBuilder.property().encodedWith(encoder);
        }
    }

    protected EntityProperty buildIdProperty(ResourceEntity<?> entity) {

        Collection<AgAttribute> ids = entity.getAgEntity().getIds();

        if (entity.getAgEntity() instanceof AgPersistentEntity) {

            // Cayenne object - PK is an ObjectId (even if it is also a
            // meaningful object property)

            if (ids.size() > 1) {
                // keeping attribute encoders in alphabetical order
                Map<String, Encoder> valueEncoders = new TreeMap<>();
                for (AgAttribute id : ids) {
                    Encoder valueEncoder = buildEncoder(id);
                    valueEncoders.put(id.getName(), valueEncoder);
                }

                return PropertyBuilder.property(getOrCreateIdReader(entity.getAgEntity()))
                        .encodedWith(new IdEncoder(valueEncoders));
            } else {

                AgAttribute id = ids.iterator().next();
                Encoder valueEncoder = buildEncoder(id);

                return PropertyBuilder.property(getOrCreateIdReader(entity.getAgEntity()))
                        .encodedWith(new IdEncoder(valueEncoder));
            }
        } else {

            // POJO - PK is an object property

            if (ids.isEmpty()) {
                // use fake ID encoder
                return PropertyBuilder.doNothingProperty();
            }

            // TODO: multi-attribute ID?

            AgAttribute id = ids.iterator().next();
            return PropertyBuilder.property(BeanPropertyReader.reader(id.getName()));
        }
    }

    /**
     * @since 2.11
     */
    protected Encoder buildEncoder(AgAttribute attribute) {
        return buildEncoder(attribute.getType());
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

    /**
     * @since 1.12
     */
    protected Encoder buildEncoder(Class<?> javaType) {
        return encodersByJavaType.computeIfAbsent(javaType, vt -> defaultEncoder);
    }

}
