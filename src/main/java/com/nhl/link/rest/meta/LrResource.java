package com.nhl.link.rest.meta;

import java.util.Collection;

public interface LrResource {

    String getPath();
    Collection<LrOperation> getOperations();
    LrEntity getEntity();

}
