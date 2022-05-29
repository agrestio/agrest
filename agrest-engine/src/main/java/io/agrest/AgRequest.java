package io.agrest;

import io.agrest.protocol.Exclude;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;

import java.util.List;

/**
 * A holder of Agrest protocol parameters for a given request.
 *
 * @since 2.13
 */
public interface AgRequest {

    List<Include> getIncludes();

    List<Exclude> getExcludes();

    Exp getExp();

    /**
     * @since 5.0
     */
    List<Sort> getSorts();

    /**
     * @deprecated since 5.0 in favor of {@link #getSorts()}
     */
    @Deprecated
    default List<Sort> getOrderings() {
        return getSorts();
    }

    String getMapBy();

    Integer getStart();

    Integer getLimit();
}
