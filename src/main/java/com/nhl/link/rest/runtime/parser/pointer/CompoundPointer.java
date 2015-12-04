package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.LinkRestException;

import javax.ws.rs.core.Response.Status;
import java.util.List;

class CompoundPointer implements LrPointer {

    private List<SimplePointer> parts;

    CompoundPointer(List<SimplePointer> parts) {

        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("Empty pointer");
        }
        this.parts = parts;
    }

    public LrPointer getLastElement() {
        return parts.get(parts.size() - 1);
    }

    @Override
    public PointerType getType() {
        return getLastElement().getType();
    }

    @Override
    public Class<?> getBaseType() {
        return parts.get(0).getBaseType();
    }

    @Override
    public Class<?> getTargetType() {
        return getLastElement().getTargetType();
    }

    @Override
    public List<? extends LrPointer> getElements() {
        return parts;
    }

    @Override
    public Object resolve(PointerContext context, Object baseObject) throws Exception {

        Object target = baseObject;

        try {
            for (SimplePointer part : parts) {
                target = part.resolve(context, target);
            }
        } catch (Exception e) {
            throw new LinkRestException(Status.BAD_REQUEST, "Failed to resolve pointer", e);
        }

        return target;
    }

    @Override
    public Object resolve(PointerContext context) throws Exception {
        return resolve(context, null);
    }

    @Override
    public String toString() {
        return getLastElement().toString();
    }
}
