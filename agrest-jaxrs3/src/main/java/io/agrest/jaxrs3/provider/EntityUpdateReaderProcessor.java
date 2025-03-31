package io.agrest.jaxrs3.provider;

import io.agrest.EntityUpdate;
import io.agrest.reflect.Types;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.protocol.IUpdateRequestParser;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;

class EntityUpdateReaderProcessor {

	private IUpdateRequestParser parser;
	private AgSchema schema;

	EntityUpdateReaderProcessor(IUpdateRequestParser parser, AgSchema schema) {
		this.parser = parser;
		this.schema = schema;
	}

	<T> Collection<EntityUpdate<T>> read(Type entityUpdateType, InputStream entityStream) {
		Class<T> typeClass = (Class<T>) Types.getClassForTypeArgument(entityUpdateType).orElse(Object.class);
		return parser.parse(schema.getEntity(typeClass), entityStream);
	}
}
