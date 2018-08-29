package com.nhl.link.rest.provider;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.protocol.IEntityUpdateParser;

import javax.ws.rs.WebApplicationException;
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

	<T> Collection<EntityUpdate<T>> read(Type entityUpdateType, InputStream entityStream) throws WebApplicationException {
		return parser.parse(metadataService.getEntityByType(entityUpdateType), entityStream);
	}
}
