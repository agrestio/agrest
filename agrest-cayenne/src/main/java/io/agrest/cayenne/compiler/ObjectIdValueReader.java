package io.agrest.cayenne.compiler;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;

public class ObjectIdValueReader implements PropertyReader {

    private static final PropertyReader instance = new ObjectIdValueReader();

    public static PropertyReader reader() {
        return instance;
    }

    @Override
    public Object value(Object root, String name) {
        ObjectId id = ((Persistent) root).getObjectId();
        if (id.isTemporary()) {
            // can only extract properties from permanent IDs
            throw new IllegalArgumentException("Can't read from temporary ObjectId: " + id);
        }

        return id.getIdSnapshot().get(name);
    }
}
