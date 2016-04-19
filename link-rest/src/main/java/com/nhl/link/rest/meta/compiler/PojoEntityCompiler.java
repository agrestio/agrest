package com.nhl.link.rest.meta.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;

/**
 * @since 1.24
 */
public class PojoEntityCompiler implements LrEntityCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PojoEntityCompiler.class);

	@Override
	public <T> LrEntity<T> compile(Class<T> type) {

		LrEntity<T> entity = LrEntityBuilder.build(type);

		// bailing on Java classes with no LR annotations
		if (entity.getIds().isEmpty() && entity.getAttributes().isEmpty() && entity.getRelationships().isEmpty()) {
			return null;
		}

		LOGGER.debug("compiling entity for type: " + type);
		return entity;
	}
}
