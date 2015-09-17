package com.nhl.link.rest;

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

    @Override
    public String toString() {
        return isCompound()? mapToString(compoundId) : id.toString();
    }

    private static String mapToString(Map<String, Object> m) {

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
