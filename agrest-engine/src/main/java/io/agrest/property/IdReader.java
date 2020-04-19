package io.agrest.property;

import java.util.Map;

/**
 * @since 3.4
 */
public interface IdReader {

    Map<String, Object> id(Object root);
}
