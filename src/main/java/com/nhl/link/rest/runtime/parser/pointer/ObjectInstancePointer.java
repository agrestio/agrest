package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.meta.LrEntity;

class ObjectInstancePointer extends SimplePointer {

    private LrEntity<?> entity;
    private Object id;

    ObjectInstancePointer(SimplePointer predecessor, LrEntity<?> entity, Object id) {
        super(predecessor, entity);
        this.entity = entity;
        // TODO: Use ID normalizers
        this.id = id;
    }

    @Override
    protected Object doResolve(PointerContext context, Object baseObject) {
        // ignoring base object
        return context.resolveObject(getTargetType(), id);
    }

    @Override
    protected String encodeToString() {
        return id.toString();
    }

    @Override
    public PointerType getType() {
        return PointerType.INSTANCE;
    }

    @Override
    public Class<?> getTargetType() {
        return entity.getType();
    }

    Object getId() {
        return id;
    }
}
