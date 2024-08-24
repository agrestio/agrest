package io.agrest.cayenne.cayenne.main.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.PersistentObject;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.SelfProperty;
import org.apache.cayenne.exp.property.StringProperty;

import io.agrest.cayenne.cayenne.main.E14;
import io.agrest.cayenne.cayenne.main.E15;
import io.agrest.cayenne.cayenne.main.E15E1;
import io.agrest.cayenne.cayenne.main.E5;

/**
 * Class _E15 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E15 extends PersistentObject {

    private static final long serialVersionUID = 1L;

    public static final SelfProperty<E15> SELF = PropertyFactory.createSelf(E15.class);

    public static final String LONG_ID_PK_COLUMN = "long_id";

    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final ListProperty<E14> E14S = PropertyFactory.createList("e14s", E14.class);
    public static final ListProperty<E15E1> E15E1 = PropertyFactory.createList("e15e1", E15E1.class);
    public static final ListProperty<E5> E5S = PropertyFactory.createList("e5s", E5.class);

    protected String name;

    protected Object e14s;
    protected Object e15e1;
    protected Object e5s;

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void addToE14s(E14 obj) {
        addToManyTarget("e14s", obj, true);
    }

    public void removeFromE14s(E14 obj) {
        removeToManyTarget("e14s", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<E14> getE14s() {
        return (List<E14>)readProperty("e14s");
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

    public void addToE5s(E5 obj) {
        addToManyTarget("e5s", obj, true);
    }

    public void removeFromE5s(E5 obj) {
        removeToManyTarget("e5s", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<E5> getE5s() {
        return (List<E5>)readProperty("e5s");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "name":
                return this.name;
            case "e14s":
                return this.e14s;
            case "e15e1":
                return this.e15e1;
            case "e5s":
                return this.e5s;
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
            case "name":
                this.name = (String)val;
                break;
            case "e14s":
                this.e14s = val;
                break;
            case "e15e1":
                this.e15e1 = val;
                break;
            case "e5s":
                this.e5s = val;
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
        out.writeObject(this.name);
        out.writeObject(this.e14s);
        out.writeObject(this.e15e1);
        out.writeObject(this.e5s);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.name = (String)in.readObject();
        this.e14s = in.readObject();
        this.e15e1 = in.readObject();
        this.e5s = in.readObject();
    }

}
