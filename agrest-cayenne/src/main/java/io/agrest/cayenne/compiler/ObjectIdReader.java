package io.agrest.cayenne.compiler;

import io.agrest.property.IdReader;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;

import java.util.HashMap;
import java.util.Map;

class ObjectIdReader implements IdReader {

    // DB name by OBJ name for a subset of ID columns whose DB names are different from OBJ names
    private final Map<String, String> idsWithNonMatchingDbNames;

    public ObjectIdReader(Map<String, String> idsWithNonMatchingDbNames) {
        this.idsWithNonMatchingDbNames = idsWithNonMatchingDbNames;
    }

    public Map<String, Object> id(Object root) {

        ObjectId id = ((Persistent) root).getObjectId();
        if (id.isTemporary()) {
            // can only extract properties from permanent IDs
            throw new IllegalArgumentException("Can't read from temporary ObjectId: " + id);
        }

        return idsWithNonMatchingDbNames.isEmpty() ? id.getIdSnapshot() : normalizeIdNames(id.getIdSnapshot());
    }

    protected Map<String, Object> normalizeIdNames(Map<String, Object> id) {
        Map<String, Object> normal = new HashMap<>(id);
        idsWithNonMatchingDbNames.forEach((k, v) -> normal.put(v, normal.remove(k)));
        return normal;
    }
}
