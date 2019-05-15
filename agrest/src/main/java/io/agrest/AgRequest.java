package io.agrest;

import io.agrest.protocol.CayenneExp;
import io.agrest.protocol.Exclude;
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

    CayenneExp getCayenneExp();

    List<Sort> getOrderings();

    String getMapBy();

    Integer getStart();

    Integer getLimit();
}
