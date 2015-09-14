package com.nhl.link.rest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IUpdateParser;

class EntityUpdateReaderProcessor {

	private IJacksonService jacksonService;
	private IUpdateParser parser;
	private IMetadataService metadataService;

	EntityUpdateReaderProcessor(IJacksonService jacksonService, IUpdateParser parser,
			IMetadataService metadataService) {
		this.parser = parser;
		this.metadataService = metadataService;
		this.jacksonService = jacksonService;
	}

	@SuppressWarnings("unchecked")
	<T> Collection<EntityUpdate<T>> read(Type entityUpdateType, InputStream entityStream)
			throws IOException, WebApplicationException {

		Class<T> entityType = (Class<T>) entityTypeForParamType(entityUpdateType);
		LrEntity<T> entity = metadataService.getLrEntity(entityType);
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"Can't parse update JSON. EntityUpdate type '" + entityType.getName() + "' is not an entity");
		}

		JsonNode json = jacksonService.parseJson(entityStream);

		return parser.parse(entity, json);
	}

	// TODO: duplication of code with ListenerInvocationFactory
	Class<?> entityTypeForParamType(Type paramType) {

		if (paramType instanceof ParameterizedType) {

			// the algorithm below is not universal. It doesn't check multiple
			// bounds...

			Type[] typeArgs = ((ParameterizedType) paramType).getActualTypeArguments();
			if (typeArgs.length == 1) {
				if (typeArgs[0] instanceof Class) {
					return (Class<?>) typeArgs[0];
				} else if (typeArgs[0] instanceof WildcardType) {
					Type[] upperBounds = ((WildcardType) typeArgs[0]).getUpperBounds();
					if (upperBounds.length == 1) {
						if (upperBounds[0] instanceof Class) {
							return (Class<?>) upperBounds[0];
						}
					}
				}
			}
		}

		return Object.class;
	}
}
