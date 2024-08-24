package io.agrest.cayenne.cayenne.main.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.PersistentObject;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.NumericProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.SelfProperty;
import org.apache.cayenne.exp.property.StringProperty;

import io.agrest.cayenne.cayenne.main.E1;
import io.agrest.cayenne.cayenne.main.E15E1;

/**
 * Class _E1 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E1 extends PersistentObject {

    private static final long serialVersionUID = 1L;

    public static final SelfProperty<E1> SELF = PropertyFactory.createSelf(E1.class);

    public static final String ID_PK_COLUMN = "id";

    public static final NumericProperty<Integer> AGE = PropertyFactory.createNumeric("age", Integer.class);
    public static final StringProperty<String> DESCRIPTION = PropertyFactory.createString("description", String.class);
    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final ListProperty<E15E1> E15E1 = PropertyFactory.createList("e15e1", E15E1.class);

    protected Integer age;
    protected String description;
    protected String name;

    protected Object e15e1;

    public void setAge(Integer age) {
        beforePropertyWrite("age", this.age, age);
        this.age = age;
    }

    public Integer getAge() {
        beforePropertyRead("age");
        return this.age;
    }

    public void setDescription(String description) {
        beforePropertyWrite("description", this.description, description);
        this.description = description;
    }

    public String getDescription() {
        beforePropertyRead("description");
        return this.description;
    }

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void addToE15e1(E15E1 obj) {
        addToManyTarget("e15e1", obj, true);
    }

    public void removeFromE15e1(E15E1 obj) {
        removeToManyTarget("e15e1", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<E15E1> getE15e1() {
        return (List<E15E1>)readProperty("e15e1");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "age":
                return this.age;
            case "description":
                return this.description;
            case "name":
                return this.name;
            case "e15e1":
                return this.e15e1;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "age":
                this.age = (Integer)val;
                break;
            case "description":
                this.description = (String)val;
                break;
            case "name":
                this.name = (String)val;
                break;
            case "e15e1":
                this.e15e1 = val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.age);
        out.writeObject(this.description);
        out.writeObject(this.name);
        out.writeObject(this.e15e1);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.age = (Integer)in.readObject();
        this.description = (String)in.readObject();
        this.name = (String)in.readObject();
        this.e15e1 = in.readObject();
    }

}
