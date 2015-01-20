package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.encoder.*;
import com.nhl.link.rest.property.BeanPropertyReader;
import com.nhl.link.rest.property.PersistentObjectIdPropertyReader;
import com.nhl.link.rest.property.PropertyBuilder;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;

import javax.ws.rs.core.Response.Status;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeEncoderFactory implements IAttributeEncoderFactory {

    static final String UTIL_DATE = Date.class.getName();
    static final String SQL_DATE = java.sql.Date.class.getName();
    static final String SQL_TIME = Time.class.getName();
    static final String SQL_TIMESTAMP = Timestamp.class.getName();

    // these are explicit overrides for named attributes
    private Map<String, EntityProperty> attributePropertiesByPath;
    private Map<String, EntityProperty> idPropertiesByEntity;

    public AttributeEncoderFactory() {
        this.attributePropertiesByPath = new ConcurrentHashMap<>();
        this.idPropertiesByEntity = new ConcurrentHashMap<>();
    }

    @Override
    public EntityProperty getAttributeProperty(ResourceEntity<?> entity, String attributeName) {
        String key = entity.getCayenneEntity().getName() + "." + attributeName;

        EntityProperty property = attributePropertiesByPath.get(key);
        if (property == null) {
            property = buildAttributeProperty(entity, attributeName);
            attributePropertiesByPath.put(key, property);
        }

        return property;
    }

    @Override
    public EntityProperty getIdProperty(ResourceEntity<?> entity) {

        String key = entity.getCayenneEntity().getName();

        EntityProperty property = idPropertiesByEntity.get(key);
        if (property == null) {
            property = buildIdProperty(entity);
            idPropertiesByEntity.put(key, property);
        }

        return property;
    }

    protected EntityProperty buildAttributeProperty(ResourceEntity<?> entity, String attributeName) {

        Encoder encoder = buildEncoder(entity.getCayenneEntity(), attributeName);
        if (DataObject.class.isAssignableFrom(entity.getType())) {
            return PropertyBuilder.dataObjectProperty().encodedWith(encoder);
        } else {
            return PropertyBuilder.property().encodedWith(encoder);
        }
    }

    protected EntityProperty buildIdProperty(ResourceEntity<?> entity) {

        // Cayenne object - PK is an ObjectId
        if (Persistent.class.isAssignableFrom(entity.getType())) {

            Collection<ObjAttribute> pks = entity.getCayenneEntity().getPrimaryKeys();
            if (pks.size() != 1) {
                String message = pks.size() == 0 ? "No pk columns" : "Multi-column PK is not supported";
                throw new IllegalArgumentException(message);
            }

            Encoder valueEncoder = buildEncoder(pks.iterator().next());

            return PropertyBuilder.property(PersistentObjectIdPropertyReader.reader()).encodedWith(
                    new ObjectIdEncoder(valueEncoder));
        }

        // POJO - PK is an object property
        Collection<String> pks = entity.getCayenneEntity().getPrimaryKeyNames();

        // compound PK entities and entities with no PK are not supported...
        if (pks.size() != 1) {
            throw new IllegalStateException(String.format("Unexpected PK size of %s for entity '%s'", entity
                    .getCayenneEntity().getName(), pks.size()));
        }

        String pkName = pks.iterator().next();
        return PropertyBuilder.property(BeanPropertyReader.reader(pkName));
    }

    protected Encoder buildEncoder(ObjEntity entity, String attributeName) {

        ObjAttribute attribute = (ObjAttribute) entity.getAttribute(attributeName);

        if (attribute == null) {
            throw new LinkRestException(Status.BAD_REQUEST, "Invalid attribute: '" + entity.getName() + "."
                    + attributeName + "'");
        }

        return buildEncoder(attribute);
    }

    /**
     * @since 1.2
     */
    protected Encoder buildEncoder(ObjAttribute attribute) {

        if (UTIL_DATE.equals(attribute.getType())) {

            int dbType = attribute.getDbAttribute().getType();
            if (dbType == Types.DATE) {
                return ISODateEncoder.encoder();
            }
            if (dbType == Types.TIME) {
                return ISOTimeEncoder.encoder();
            } else {
                // JDBC TIMESTAMP or something entirely unrecognized
                return ISODateTimeEncoder.encoder();
            }
        }
        // less common cases of mapping to java.sql.* types...
        else if (SQL_TIMESTAMP.equals(attribute.getType())) {
            return ISODateTimeEncoder.encoder();
        } else if (SQL_DATE.equals(attribute.getType())) {
            return ISODateEncoder.encoder();
        } else if (SQL_TIME.equals(attribute.getType())) {
            return ISOTimeEncoder.encoder();
        }

        return GenericEncoder.encoder();
    }

}
