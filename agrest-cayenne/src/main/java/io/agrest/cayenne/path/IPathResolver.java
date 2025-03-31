package io.agrest.cayenne.path;

/**
 * Resolves Agrest expression paths to Cayenne paths.
 *
 * @since 5.0
 */
public interface IPathResolver {

    PathDescriptor resolve(String entityName, String agPath);
}
