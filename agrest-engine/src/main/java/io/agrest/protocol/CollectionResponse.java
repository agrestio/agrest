package io.agrest.protocol;

import java.util.List;

/**
 * Represents a "Collection Response" document from the Agrest Protocol.
 *
 * @since 5.0
 */
public interface CollectionResponse<T> {

    /**
     * Returns a collection of root objects of the response.
     */
    List<? extends T> getData();

    /**
     * @return a total count of objects in the resource, which is greater or equal of the number of objects in this
     * response.
     */
    int getTotal();
}
