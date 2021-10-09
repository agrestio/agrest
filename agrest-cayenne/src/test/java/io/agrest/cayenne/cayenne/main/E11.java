package io.agrest.cayenne.cayenne.main;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.cayenne.cayenne.main.auto._E11;
import org.apache.cayenne.ObjectId;

public class E11 extends _E11 {

    @Override
    @AgId(readable = false, writable = false)
    public ObjectId getObjectId() {
        return super.getObjectId();
    }

    @Override
    @AgAttribute(writable = false)
    public String getAddress() {
        return super.getAddress();
    }

    @Override
    @AgAttribute(writable = false, readable = false)
    public String getName() {
        return super.getName();
    }

    @Override
    @AgRelationship(writable = false)
    public E10 getE10() {
        return super.getE10();
    }
}
