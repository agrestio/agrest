package com.nhl.link.rest.runtime.encoder;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.map.ObjAttribute;

import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.GenericEncoder;
import com.nhl.link.rest.encoder.ISODateEncoder;
import com.nhl.link.rest.encoder.ISODateTimeEncoder;
import com.nhl.link.rest.encoder.ISOTimeEncoder;
import com.nhl.link.rest.encoder.ObjectIdEncoder;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrPersistentAttribute;
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
	public EntityProperty getAttributeProperty(ResourceEntity<?> entity, LrAttribute attribute) {
		String key = entity.getLrEntity().getName() + "." + attribute.getName();

		EntityProperty property = attributePropertiesByPath.get(key);
		if (property == null) {
			property = buildAttributeProperty(entity, attribute);
			attributePropertiesByPath.put(key, property);
		}

		return property;
	}

	@Override
	public EntityProperty getIdProperty(ResourceEntity<?> entity) {

		String key = entity.getLrEntity().getName();

		EntityProperty property = idPropertiesByEntity.get(key);
		if (property == null) {
			property = buildIdProperty(entity);
			idPropertiesByEntity.put(key, property);
		}

		return property;
	}

	protected EntityProperty buildAttributeProperty(ResourceEntity<?> entity, LrAttribute attribute) {
		
		boolean persistent = attribute instanceof LrPersistentAttribute;
		
		int jdbcType = persistent ? ((LrPersistentAttribute) attribute).getJdbcType() : Integer.MIN_VALUE;

		Encoder encoder = buildEncoder(attribute.getJavaType(), jdbcType);
		if (persistent && DataObject.class.isAssignableFrom(entity.getType())) {
			return PropertyBuilder.dataObjectProperty().encodedWith(encoder);
		} else {
			return PropertyBuilder.property().encodedWith(encoder);
		}
	}

	protected EntityProperty buildIdProperty(ResourceEntity<?> entity) {

		// Cayenne object - PK is an ObjectId
		if (Persistent.class.isAssignableFrom(entity.getType())) {

			Collection<ObjAttribute> pks = entity.getLrEntity().getObjEntity().getPrimaryKeys();
			if (pks.size() != 1) {
				String message = pks.size() == 0 ? "No pk columns" : "Multi-column PK is not supported";
				throw new IllegalArgumentException(message);
			}
			
			ObjAttribute attribute = pks.iterator().next();
			int dbType = attribute.getDbAttribute() != null ? attribute.getDbAttribute().getType() : Integer.MIN_VALUE;
			Encoder valueEncoder = buildEncoder(attribute.getType(), dbType);

			return PropertyBuilder.property(PersistentObjectIdPropertyReader.reader()).encodedWith(
					new ObjectIdEncoder(valueEncoder));
		}

		// POJO - PK is an object property
		Collection<String> pks = entity.getLrEntity().getObjEntity().getPrimaryKeyNames();

		// compound PK entities and entities with no PK are not supported...
		if (pks.size() != 1) {
			throw new IllegalStateException(String.format("Unexpected PK size of %s for entity '%s'", entity
					.getLrEntity().getName(), pks.size()));
		}

		String pkName = pks.iterator().next();
		return PropertyBuilder.property(BeanPropertyReader.reader(pkName));
	}

	/**
	 * @since 1.12
	 */
	protected Encoder buildEncoder(String javaType, int jdbcType) {

		if (UTIL_DATE.equals(javaType)) {
			if (jdbcType == Types.DATE) {
				return ISODateEncoder.encoder();
			}
			if (jdbcType == Types.TIME) {
				return ISOTimeEncoder.encoder();
			} else {
				// JDBC TIMESTAMP or something entirely unrecognized
				return ISODateTimeEncoder.encoder();
			}
		}
		// less common cases of mapping to java.sql.* types...
		else if (SQL_TIMESTAMP.equals(javaType)) {
			return ISODateTimeEncoder.encoder();
		} else if (SQL_DATE.equals(javaType)) {
			return ISODateEncoder.encoder();
		} else if (SQL_TIME.equals(javaType)) {
			return ISOTimeEncoder.encoder();
		}

		return GenericEncoder.encoder();
	}

}
