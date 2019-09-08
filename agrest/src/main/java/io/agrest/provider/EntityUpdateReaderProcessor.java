package io.agrest.provider;

import io.agrest.EntityUpdate;
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
		return parser.parse(metadataService.getAgEntityByType(entityUpdateType), entityStream);
	}
}
