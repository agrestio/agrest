package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.LinkRestException;

import javax.ws.rs.core.Response.Status;
import java.util.List;

public class CompoundPointer implements LrPointer {

    private List<LrPointer> parts;

    CompoundPointer(List<LrPointer> parts) {
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
    public Class<?> getTargetType() {
        return getLastElement().getTargetType();
    }

    @Override
    public Object resolve(Object context) {

        Object target = null;

        try {
            for (LrPointer part : parts) {
                target = part.resolve(target);
            }
        } catch (Exception e) {
            throw new LinkRestException(Status.BAD_REQUEST, "Failed to resolve pointer", e);
        }

        return target;
    }

    @Override
    public Object resolve() throws Exception {
        return resolve(null);
    }

    @Override
    public String toString() {
        return getLastElement().toString();
    }
}
