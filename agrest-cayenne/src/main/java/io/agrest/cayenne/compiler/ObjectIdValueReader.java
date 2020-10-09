package io.agrest.cayenne.compiler;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;

public class ObjectIdValueReader implements PropertyReader {

    private final String snapshotKey;

    public static PropertyReader reader(String snapshotKey) {
        return new ObjectIdValueReader(snapshotKey);
    }

    public ObjectIdValueReader(String snapshotKey) {
        this.snapshotKey = snapshotKey;
    }

    @Override
    public Object value(Object object) {
        ObjectId id = ((Persistent) object).getObjectId();
        if (id.isTemporary()) {
            // can only extract properties from permanent IDs
            throw new IllegalArgumentException("Can't read from temporary ObjectId: " + id);
        }

        return id.getIdSnapshot().get(snapshotKey);
    }
}
