package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * @since 1.24
 */
public class PojoEntityCompiler implements LrEntityCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PojoEntityCompiler.class);

	private IJsonValueConverterFactory converterFactory;

	public PojoEntityCompiler(@Inject IJsonValueConverterFactory converterFactory) {
		this.converterFactory = converterFactory;
	}

	@Override
	public <T> LrEntity<T> compile(Class<T> type, LrDataMap dataMap) {
		return new LazyLrEntity<>(type, () -> doCompile(type, dataMap));
	}

	private <T> LrEntity<T> doCompile(Class<T> type, LrDataMap dataMap) {

		LOGGER.debug("compiling entity for type: " + type);
		LrEntity<T> entity = new LrEntityBuilder<>(type, dataMap, converterFactory).build();

		// bailing on Java classes with no LR annotations
		if (entity.getIds().isEmpty() && entity.getAttributes().isEmpty() && entity.getRelationships().isEmpty()) {
			throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR, "Not an entity: " + type.getName());
		}
		return entity;
	}
}
