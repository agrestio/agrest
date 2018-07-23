package com.nhl.link.rest.runtime.parser.entityupdate;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.meta.LrEntity;

import java.io.InputStream;
import java.util.Collection;

/**
 * @since 1.20
 */
public interface IEntityUpdateParser {

	<T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, String entityData);

	<T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, InputStream entityStream);
}
