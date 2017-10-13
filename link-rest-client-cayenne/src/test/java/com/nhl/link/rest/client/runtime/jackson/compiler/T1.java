package com.nhl.link.rest.client.runtime.jackson.compiler;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

public class T1 extends CayenneDataObject {
    public static final Property<Integer> P_INT = Property.create("pInt", Integer.class);
    public static final Property<String> P_STRING = Property.create("pString", String.class);

    public Integer getPInt() {
        return (Integer) readPropertyDirectly(P_INT.getName());
    }

    public String getPString() {
        return (String) readPropertyDirectly(P_STRING.getName());
    }
}
