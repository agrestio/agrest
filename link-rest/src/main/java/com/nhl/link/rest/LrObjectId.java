package com.nhl.link.rest;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.parser.converter.Normalizer;

import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LrObjectId {

    private Object id;
    private Map<String, Object> compoundId;

    public LrObjectId(Object id) {
        this.id = id;
    }

    public LrObjectId(Map<String, Object> compoundId) {
        this.compoundId = compoundId;
    }

    public boolean isCompound() {
        return compoundId != null;
    }

    public Object get(String attributeName) {
        return isCompound()? compoundId.get(attributeName) : id;
    }

    /**
     * @return Original ID value, that was used to create this wrapper ID
     */
    public Object get() {
        return isCompound()? compoundId : id;
    }

    public int size() {
        return isCompound()? compoundId.size() : 1;
    }

    public Map<String, Object> asMap(LrEntity<?> entity) {

        if (entity == null) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                    "Can't build ID: entity is null");
        }

        Map<String, Object> idMap = new HashMap<>();
        Collection<LrAttribute> idAttributes = entity.getIds();
        if (idAttributes.size() != size()) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Wrong ID size: expected " + idAttributes.size() + ", got: " + size());
        }

        if (isCompound()) {
            for (LrAttribute idAttribute : idAttributes) {
                Object idValue = Normalizer.normalize(compoundId.get(idAttribute.getName()), idAttribute.getType());
                if (idValue == null) {
                    throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                            "Failed to build a compound ID for entity " + entity.getName()
                                    + ": one of the entity's ID parts is missing in this ID object: " + idAttribute.getName());
                }
                if (idAttribute instanceof LrPersistentAttribute) {
                    idMap.put(((LrPersistentAttribute) idAttribute).getDbAttribute().getName(), idValue);
                } else {
                    idMap.put(idAttribute.getName(), idValue);
                }
           }
        } else {
            LrAttribute idAttribute = idAttributes.iterator().next();
            idMap.put(idAttribute.getName(), Normalizer.normalize(id, idAttribute.getType()));
        }
        return idMap;
    }

    @Override
    public String toString() {
        return isCompound()? mapToString(compoundId) : id.toString();
    }

    // TODO: move this somewhere?
    public static String mapToString(Map<String, Object> m) {

        StringBuilder buf = new StringBuilder("{");

        Iterator<Map.Entry<String, Object>> iterator = m.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();

            buf.append(entry.getKey());
            buf.append(":");
            buf.append(entry.getValue());

            if (iterator.hasNext()) {
                buf.append(",");
            }
        }
        buf.append("}");

        return buf.toString();
    }

}
