package com.nhl.link.rest.meta;

import java.util.Collection;

public interface LrResource<T> {

    String getPath();
    Collection<LrOperation> getOperations();
    LrEntity<T> getEntity();

}
