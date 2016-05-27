package com.nhl.link.rest.it.fixture.cayenne.auto;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.it.fixture.cayenne.E22;

/**
 * Class _E23 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E23 extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "id";

    public static final Property<String> NAME = new Property<String>("name");
    public static final Property<E22> E22 = new Property<E22>("e22");

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void setE22(E22 e22) {
        setToOneTarget("e22", e22, true);
    }

    public E22 getE22() {
        return (E22)readProperty("e22");
    }


}