package io.agrest.provider;

import io.agrest.EntityUpdate;
import io.agrest.base.reflect.Types;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.protocol.IEntityUpdateParser;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;

class EntityUpdateReaderProcessor {

	private IEntityUpdateParser parser;
	private IMetadataService metadataService;

	EntityUpdateReaderProcessor(IEntityUpdateParser parser, IMetadataService metadataService) {
		this.parser = parser;
		this.metadataService = metadataService;
	}

	<T> Collection<EntityUpdate<T>> read(Type entityUpdateType, InputStream entityStream) {
		Class<T> typeClass = (Class<T>) Types.getClassForTypeArgument(entityUpdateType).orElse(Object.class);
		return parser.parse(metadataService.getAgEntity(typeClass), entityStream);
	}
}
