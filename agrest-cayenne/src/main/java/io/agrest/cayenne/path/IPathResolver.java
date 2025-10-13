package io.agrest.cayenne.path;

import java.util.Collections;
import java.util.Map;

/**
 * Resolves Agrest expression paths to Cayenne paths.
 *
 * @since 5.0
 */
public interface IPathResolver {

    default PathDescriptor resolve(String entityName, String agPath) {
        return resolve(entityName, agPath, Collections.emptyMap());
    }

    PathDescriptor resolve(String entityName, String agPath, Map<String, String> aliases);
}
