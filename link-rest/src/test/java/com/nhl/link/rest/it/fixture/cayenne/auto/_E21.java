package com.nhl.link.rest.it.fixture.cayenne.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.it.fixture.cayenne.E20;

/**
 * Class _E21 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E21 extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "id";

    public static final Property<Integer> AGE = Property.create("age", Integer.class);
    public static final Property<String> DESCRIPTION = Property.create("description", String.class);
    public static final Property<String> NAME = Property.create("name", String.class);
    public static final Property<List<E20>> E20S = Property.create("e20s", List.class);

    public void setAge(Integer age) {
        writeProperty("age", age);
    }
    public Integer getAge() {
        return (Integer)readProperty("age");
    }

    public void setDescription(String description) {
        writeProperty("description", description);
    }
    public String getDescription() {
        return (String)readProperty("description");
    }

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void addToE20s(E20 obj) {
        addToManyTarget("e20s", obj, true);
    }
    public void removeFromE20s(E20 obj) {
        removeToManyTarget("e20s", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<E20> getE20s() {
        return (List<E20>)readProperty("e20s");
    }


}