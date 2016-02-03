package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.meta.LrEntity;

import java.lang.reflect.Array;
import java.util.Collection;

public class EntityCollectionPointer extends SimplePointer {

    private LrEntity<?> entity;

    EntityCollectionPointer(SimplePointer predecessor, LrEntity<?> entity) {
        super(predecessor, entity);

        this.entity = entity;
    }

    @Override
    protected Object doResolve(PointerContext context, Object baseObject) {
        // ignoring base object
        return context.resolveAll(entity.getType());
    }

    @Override
    protected void doUpdate(PointerContext context, Object baseObject, Object value) {
        // ignoring base object
        if (value.getClass().isArray()) {
            if (Array.getLength(value) == 0) {
                context.removeAll(entity.getType());
            } else {
                throw new RuntimeException("Multi-valued updates not implemented yet");
            }
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            if (((Collection<?>) value).isEmpty()) {
                context.removeAll(entity.getType());
            } else {
                throw new RuntimeException("Multi-valued updates not implemented yet");
            }
        } else {
            context.addObject(value);
        }
    }

    @Override
    protected void doDelete(PointerContext context, Object baseObject) {
        // ignoring base object
        context.removeAll(entity.getType());
    }

    @Override
    protected String encodeToString() {
        return entity.getName();
    }

    @Override
    public PointerType getType() {
        return PointerType.ENTITY_COLLECTION;
    }

    @Override
    public Class<?> getTargetType() {
        return entity.getType();
    }
}
