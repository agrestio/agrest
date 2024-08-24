package io.agrest.cayenne.cayenne.main.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.PersistentObject;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.SelfProperty;

import io.agrest.cayenne.cayenne.main.E12E13;
import io.agrest.cayenne.cayenne.main.E13;

/**
 * Class _E13 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E13 extends PersistentObject {

    private static final long serialVersionUID = 1L;

    public static final SelfProperty<E13> SELF = PropertyFactory.createSelf(E13.class);

    public static final String ID_PK_COLUMN = "id";

    public static final ListProperty<E12E13> E1213 = PropertyFactory.createList("e1213", E12E13.class);


    protected Object e1213;

    public void addToE1213(E12E13 obj) {
        addToManyTarget("e1213", obj, true);
    }

    public void removeFromE1213(E12E13 obj) {
        removeToManyTarget("e1213", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<E12E13> getE1213() {
        return (List<E12E13>)readProperty("e1213");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "e1213":
                return this.e1213;
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
            case "e1213":
                this.e1213 = val;
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
        out.writeObject(this.e1213);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.e1213 = in.readObject();
    }

}
