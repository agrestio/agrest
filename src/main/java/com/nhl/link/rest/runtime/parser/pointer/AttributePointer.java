package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;

import javax.ws.rs.core.Response;

class AttributePointer extends SimplePointer {

    private LrAttribute attribute;

    AttributePointer(SimplePointer predecessor, LrEntity<?> entity, LrAttribute attribute) {
        super(predecessor, entity);
        this.attribute = attribute;
    }

    @Override
    protected String encodeToString() {
        return attribute.getName();
    }

    @Override
    public PointerType getType() {
        return PointerType.ATTRIBUTE;
    }

    @Override
    public Class<?> getTargetType() {
        try {
            // TODO: Primitive types mapping
            return Class.forName(attribute.getJavaType());
        } catch (ClassNotFoundException e) {
            throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Unknown attribute type: " + attribute.getJavaType());
        }
    }

    LrAttribute getAttribute() {
        return attribute;
    }
}
