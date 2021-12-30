package io.agrest.cayenne.path;

import io.agrest.meta.AgEntity;


/**
 * Resolves Agrest expression paths to Cayenne paths.
 *
 * @since 5.0
 */
public interface IPathResolver {

    PathDescriptor resolve(AgEntity<?> entity, String agPath);
}
