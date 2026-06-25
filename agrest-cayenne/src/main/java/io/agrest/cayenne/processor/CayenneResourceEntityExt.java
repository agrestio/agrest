package io.agrest.cayenne.processor;

import org.apache.cayenne.query.FluentSelect;

/**
 * @since 5.0
 */
public interface CayenneResourceEntityExt {

    FluentSelect<?, ?> getSelect();
}
