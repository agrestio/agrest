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

import io.agrest.cayenne.cayenne.main.E29;
import io.agrest.cayenne.cayenne.main.E30;

/**
 * Class _E29 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E29 extends PersistentObject {

    private static final long serialVersionUID = 1L;

    public static final SelfProperty<E29> SELF = PropertyFactory.createSelf(E29.class);

    public static final String ID1_PK_COLUMN = "id1";
    public static final String ID2_PK_COLUMN = "id2";

    public static final NumericProperty<Integer> ID2PROP = PropertyFactory.createNumeric("id2Prop", Integer.class);
    public static final ListProperty<E30> E30S = PropertyFactory.createList("e30s", E30.class);

    protected Integer id2Prop;

    protected Object e30s;

    public void setId2Prop(Integer id2Prop) {
        beforePropertyWrite("id2Prop", this.id2Prop, id2Prop);
        this.id2Prop = id2Prop;
    }

    public Integer getId2Prop() {
        beforePropertyRead("id2Prop");
        return this.id2Prop;
    }

    public void addToE30s(E30 obj) {
        addToManyTarget("e30s", obj, true);
    }

    public void removeFromE30s(E30 obj) {
        removeToManyTarget("e30s", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<E30> getE30s() {
        return (List<E30>)readProperty("e30s");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "id2Prop":
                return this.id2Prop;
            case "e30s":
                return this.e30s;
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
            case "id2Prop":
                this.id2Prop = (Integer)val;
                break;
            case "e30s":
                this.e30s = val;
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
        out.writeObject(this.id2Prop);
        out.writeObject(this.e30s);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.id2Prop = (Integer)in.readObject();
        this.e30s = in.readObject();
    }

}
