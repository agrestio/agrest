package com.nhl.link.rest.meta;

import java.util.Collection;

public interface LrResource<T> {

    String getPath();
    LinkType getType();
    Collection<LrOperation> getOperations();
    LrEntity<T> getEntity();

}
