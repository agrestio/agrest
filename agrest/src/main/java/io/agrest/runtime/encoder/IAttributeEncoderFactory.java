package io.agrest.runtime.encoder;

import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Provides an extension point for building custom attribute encoders if the
 * default encoders based on the ORM model are not good enough for any reason.
 */
public interface IAttributeEncoderFactory {

	static final Class<?> UTIL_DATE = Date.class;
	static final Class<?> SQL_DATE = java.sql.Date.class;
	static final Class<?> SQL_TIME = Time.class;
	static final Class<?> SQL_TIMESTAMP = Timestamp.class;
	static final Class<?> LOCAL_DATE = LocalDate.class;
	static final Class<?> LOCAL_TIME = LocalTime.class;
	static final Class<?> LOCAL_DATETIME = LocalDateTime.class;
	static final Class<?> OFFSET_DATETIME = OffsetDateTime.class;

	/**
	 * @since 1.23
	 */
	EntityProperty getAttributeProperty(AgEntity<?> entity, AgAttribute attribute);

	/**
	 * @since 1.23
	 */
	EntityProperty getRelationshipProperty(AgEntity<?> entity, AgRelationship relationship, Encoder encoder);

	EntityProperty getIdProperty(ResourceEntity<?, ?> entity);
}
