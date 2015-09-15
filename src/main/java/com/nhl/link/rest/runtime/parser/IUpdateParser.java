package com.nhl.link.rest.runtime.parser;

import java.io.InputStream;
import java.util.Collection;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.meta.LrEntity;

/**
 * @since 1.20
 */
public interface IUpdateParser {

	<T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, String entityData);

	<T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, InputStream entityStream);
}
