package io.agrest.cayenne.cayenne.main.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;

import org.apache.cayenne.PersistentObject;
import org.apache.cayenne.exp.property.DateProperty;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.SelfProperty;
import org.apache.cayenne.exp.property.StringProperty;

import io.agrest.cayenne.cayenne.main.E15;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;

/**
 * Class _E5 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E5 extends PersistentObject {

    private static final long serialVersionUID = 1L;

    public static final SelfProperty<E5> SELF = PropertyFactory.createSelf(E5.class);

    public static final String ID_PK_COLUMN = "id";

    public static final DateProperty<Date> DATE = PropertyFactory.createDate("date", Date.class);
    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final ListProperty<E15> E15S = PropertyFactory.createList("e15s", E15.class);
    public static final ListProperty<E3> E3S = PropertyFactory.createList("e3s", E3.class);

    protected Date date;
    protected String name;

    protected Object e15s;
    protected Object e3s;

    public void setDate(Date date) {
        beforePropertyWrite("date", this.date, date);
        this.date = date;
    }

    public Date getDate() {
        beforePropertyRead("date");
        return this.date;
    }

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void addToE15s(E15 obj) {
        addToManyTarget("e15s", obj, true);
    }

    public void removeFromE15s(E15 obj) {
        removeToManyTarget("e15s", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<E15> getE15s() {
        return (List<E15>)readProperty("e15s");
    }

    public void addToE3s(E3 obj) {
        addToManyTarget("e3s", obj, true);
    }

    public void removeFromE3s(E3 obj) {
        removeToManyTarget("e3s", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<E3> getE3s() {
        return (List<E3>)readProperty("e3s");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "date":
                return this.date;
            case "name":
                return this.name;
            case "e15s":
                return this.e15s;
            case "e3s":
                return this.e3s;
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
            case "date":
                this.date = (Date)val;
                break;
            case "name":
                this.name = (String)val;
                break;
            case "e15s":
                this.e15s = val;
                break;
            case "e3s":
                this.e3s = val;
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
        out.writeObject(this.date);
        out.writeObject(this.name);
        out.writeObject(this.e15s);
        out.writeObject(this.e3s);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.date = (Date)in.readObject();
        this.name = (String)in.readObject();
        this.e15s = in.readObject();
        this.e3s = in.readObject();
    }

}
