package com.nhl.link.rest.runtime.encoder;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.nhl.link.rest.property.IdPropertyReader;
import org.apache.cayenne.DataObject;

import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.GenericEncoder;
import com.nhl.link.rest.encoder.ISODateEncoder;
import com.nhl.link.rest.encoder.ISODateTimeEncoder;
import com.nhl.link.rest.encoder.ISOTimeEncoder;
import com.nhl.link.rest.encoder.IdEncoder;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.cayenne.CayenneLrEntity;
import com.nhl.link.rest.property.BeanPropertyReader;
import com.nhl.link.rest.property.PropertyBuilder;

public class AttributeEncoderFactory implements IAttributeEncoderFactory {

	static final Class<?> UTIL_DATE = Date.class;
	static final Class<?> SQL_DATE = java.sql.Date.class;
	static final Class<?> SQL_TIME = Time.class;
	static final Class<?> SQL_TIMESTAMP = Timestamp.class;

	// these are explicit overrides for named attributes
	private Map<String, EntityProperty> attributePropertiesByPath;
	private Map<String, EntityProperty> idPropertiesByEntity;
	private Map<LrEntity<?>, IdPropertyReader> idPropertyReaders;

	public AttributeEncoderFactory() {
		this.attributePropertiesByPath = new ConcurrentHashMap<>();
		this.idPropertiesByEntity = new ConcurrentHashMap<>();
		this.idPropertyReaders = new ConcurrentHashMap<>();
	}

	@Override
	public EntityProperty getAttributeProperty(LrEntity<?> entity, LrAttribute attribute) {
		String key = entity.getName() + "." + attribute.getName();

		EntityProperty property = attributePropertiesByPath.get(key);
		if (property == null) {
			property = buildAttributeProperty(entity, attribute);
			attributePropertiesByPath.put(key, property);
		}

		return property;
	}

	@Override
	public EntityProperty getRelationshipProperty(LrEntity<?> entity, LrRelationship relationship, Encoder encoder) {

		// TODO: can't cache, as target encoder is dynamic...
		if (DataObject.class.isAssignableFrom(entity.getType())) {
			return PropertyBuilder.dataObjectProperty().encodedWith(encoder);
		} else {
			return PropertyBuilder.property().encodedWith(encoder);
		}
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

	protected EntityProperty buildAttributeProperty(LrEntity<?> entity, LrAttribute attribute) {

		boolean persistent = attribute instanceof LrPersistentAttribute;

		int jdbcType = persistent ? ((LrPersistentAttribute) attribute).getJdbcType() : Integer.MIN_VALUE;

		Encoder encoder = buildEncoder(attribute.getType(), jdbcType);
		if (persistent && DataObject.class.isAssignableFrom(entity.getType())) {
			return PropertyBuilder.dataObjectProperty().encodedWith(encoder);
		} else {
			return PropertyBuilder.property().encodedWith(encoder);
		}
	}

	protected EntityProperty buildIdProperty(ResourceEntity<?> entity) {

		Collection<LrAttribute> ids = entity.getLrEntity().getIds();

		if (entity.getLrEntity() instanceof CayenneLrEntity) {

			// Cayenne object - PK is an ObjectId (even if it is also a
			// meaningful object property)

			if (ids.size() > 1) {
				// keeping attribute encoders in alphabetical order
				Map<String, Encoder> valueEncoders = new TreeMap<>();
				for (LrAttribute id : ids) {
					Encoder valueEncoder = buildEncoder(id.getType(), getJdbcType(id));
					valueEncoders.put(id.getName(), valueEncoder);
				}

				return PropertyBuilder.property(getOrCreateIdPropertyReader(entity.getLrEntity()))
						.encodedWith(new IdEncoder(valueEncoders));
			} else {

				LrAttribute id = ids.iterator().next();
				Encoder valueEncoder = buildEncoder(id.getType(), getJdbcType(id));

				return PropertyBuilder.property(getOrCreateIdPropertyReader(entity.getLrEntity()))
						.encodedWith(new IdEncoder(valueEncoder));
			}
		} else {

			// POJO - PK is an object property

			if (ids.isEmpty()) {
				// use fake ID encoder
				return PropertyBuilder.doNothingProperty();
			}

			// TODO: multi-attribute ID?

			LrAttribute id = ids.iterator().next();
			return PropertyBuilder.property(BeanPropertyReader.reader(id.getName()));
		}
	}

	private int getJdbcType(LrAttribute attribute) {
		if (attribute instanceof LrPersistentAttribute) {
			return ((LrPersistentAttribute) attribute).getJdbcType();
		} else {
			return Integer.MIN_VALUE;
		}
	}

	private IdPropertyReader getOrCreateIdPropertyReader(LrEntity<?> entity) {

		IdPropertyReader reader = idPropertyReaders.get(entity);
		if (reader == null) {
			reader = new IdPropertyReader(entity);
			IdPropertyReader oldReader = idPropertyReaders.putIfAbsent(entity, reader);
			reader = (oldReader == null)? reader : oldReader;
		}
		return reader;
	}

	/**
	 * @since 1.12
	 */
	protected Encoder buildEncoder(Class<?> javaType, int jdbcType) {

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
