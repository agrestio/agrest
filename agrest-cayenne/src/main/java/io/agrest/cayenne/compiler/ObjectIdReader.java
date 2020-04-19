package io.agrest.cayenne.compiler;

import io.agrest.property.IdReader;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;

import java.util.Map;

class ObjectIdReader implements IdReader {

    private static final IdReader instance = new ObjectIdReader();

    public static IdReader reader() {
        return instance;
    }

    public Map<String, Object> id(Object root) {

        ObjectId id = ((Persistent) root).getObjectId();
        if (id.isTemporary()) {
            // can only extract properties from permanent IDs
            throw new IllegalArgumentException("Can't read from temporary ObjectId: " + id);
        }

        return id.getIdSnapshot();
    }
}
