package io.agrest.jaxrs.provider;

import io.agrest.EntityUpdate;
import io.agrest.reflect.Types;
import io.agrest.meta.AgDataMap;
import io.agrest.runtime.protocol.IEntityUpdateParser;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;

class EntityUpdateReaderProcessor {

	private IEntityUpdateParser parser;
	private AgDataMap dataMap;

	EntityUpdateReaderProcessor(IEntityUpdateParser parser, AgDataMap dataMap) {
		this.parser = parser;
		this.dataMap = dataMap;
	}

	<T> Collection<EntityUpdate<T>> read(Type entityUpdateType, InputStream entityStream) {
		Class<T> typeClass = (Class<T>) Types.getClassForTypeArgument(entityUpdateType).orElse(Object.class);
		return parser.parse(dataMap.getEntity(typeClass), entityStream);
	}
}
