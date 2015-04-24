package com.nhl.link.rest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.cayenne.CayenneLrAttribute;

import java.io.IOException;

public abstract class PropertyMetadataEncoder extends AbstractEncoder {

    private static final Encoder instance = new PropertyMetadataEncoder() {

        @Override
        protected String getPropertyName(Object property) {
            if (property instanceof LrAttribute) {
                return ((LrAttribute)property).getName();
            } else if (property instanceof LrRelationship) {
                return ((LrRelationship)property).getName();
            } else {
                return null;
            }
        }

        @Override
        protected String getPropertyType(Object property) {
            return null;
        }

        @Override
        protected void doEncode(Object property, JsonGenerator out) throws IOException {
            if (property instanceof CayenneLrAttribute) {
                if (((CayenneLrAttribute)property).getDbAttribute().isMandatory()) {
                    out.writeBooleanField("mandatory", true);
                }
            } else if (property instanceof LrRelationship) {
                out.writeBooleanField("relationship", true);
                if (((LrRelationship)property).isToMany()) {
                    out.writeBooleanField("collection", true);
                }
            }
        }

    };

    public static Encoder encoder() {
        return instance;
    }

    @Override
    protected boolean encodeNonNullObject(Object property, JsonGenerator out) throws IOException {
        if (property == null) {
            return false;
        }

        out.writeStartObject();

        out.writeStringField("name", getPropertyName(property));
        out.writeStringField("type", getPropertyType(property));

        doEncode(property, out);

        out.writeEndObject();

        return true;
    }

    protected abstract String getPropertyName(Object property);
    protected abstract String getPropertyType(Object property);
    protected abstract void doEncode(Object object, JsonGenerator out) throws IOException;

}
