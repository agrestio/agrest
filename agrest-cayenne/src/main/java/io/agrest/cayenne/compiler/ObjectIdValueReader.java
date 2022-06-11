package io.agrest.cayenne.compiler;

import io.agrest.reader.DataReader;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;

public class ObjectIdValueReader implements DataReader {

    private final String snapshotKey;

    public static DataReader reader(String snapshotKey) {
        return new ObjectIdValueReader(snapshotKey);
    }

    public ObjectIdValueReader(String snapshotKey) {
        this.snapshotKey = snapshotKey;
    }

    @Override
    public Object read(Object object) {
        ObjectId id = ((Persistent) object).getObjectId();
        if (id.isTemporary()) {
            // can only extract properties from permanent IDs
            throw new IllegalArgumentException("Can't read from temporary ObjectId: " + id);
        }

        return id.getIdSnapshot().get(snapshotKey);
    }
}
