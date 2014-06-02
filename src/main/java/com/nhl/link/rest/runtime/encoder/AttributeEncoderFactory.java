package com.nhl.link.rest.runtime.encoder;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.Entity;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.GenericEncoder;
import com.nhl.link.rest.encoder.ISODateEncoder;
import com.nhl.link.rest.encoder.ISODateTimeEncoder;
import com.nhl.link.rest.encoder.ISOTimeEncoder;
import com.nhl.link.rest.encoder.NumericObjectIdEncoder;
import com.nhl.link.rest.property.BeanPropertyReader;
import com.nhl.link.rest.property.PersistentObjectIdPropertyReader;
import com.nhl.link.rest.property.PropertyBuilder;

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
	public EntityProperty getAttributeProperty(Entity<?> entity, String attributeName) {
		String key = entity.getEntity().getName() + "." + attributeName;

		EntityProperty property = attributePropertiesByPath.get(key);
		if (property == null) {
			property = buildAttributeProperty(entity, attributeName);
			attributePropertiesByPath.put(key, property);
		}

		return property;
	}

	@Override
	public EntityProperty getIdProperty(Entity<?> entity) {

		String key = entity.getEntity().getName();

		EntityProperty property = idPropertiesByEntity.get(key);
		if (property == null) {
			property = buildIdProperty(entity);
			idPropertiesByEntity.put(key, property);
		}

		return property;
	}

	protected EntityProperty buildAttributeProperty(Entity<?> entity, String attributeName) {

		Encoder encoder = buildEncoder(entity.getEntity(), attributeName);
		if (DataObject.class.isAssignableFrom(entity.getType())) {
			return PropertyBuilder.dataObjectProperty().encodedWith(encoder);
		} else {
			return PropertyBuilder.property().encodedWith(encoder);
		}
	}

	protected EntityProperty buildIdProperty(Entity<?> entity) {
		if (Persistent.class.isAssignableFrom(entity.getType())) {
			// ignoring compound PK entities; ignoring non-numeric PK entities
			return PropertyBuilder.property(PersistentObjectIdPropertyReader.reader()).encodedWith(
					NumericObjectIdEncoder.encoder());
		}

		Collection<String> pks = entity.getEntity().getPrimaryKeyNames();

		// compound PK entities and entities with no PK are not supported...
		if (pks.size() != 1) {
			throw new IllegalStateException(String.format("Unexpected PK size of %s for entity '%s'", entity
					.getEntity().getName(), pks.size()));
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
