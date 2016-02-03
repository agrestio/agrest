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
        return context.resolveObject(entity.getType(), id);
    }

    @Override
    protected void doUpdate(PointerContext context, Object baseObject, Object value) {
        // ignoring base object (can be null if the pointer is, say, "3")
        context.updateObject(entity.getType(), id, value);
    }

    @Override
    protected void doDelete(PointerContext context, Object baseObject) {
        // ignoring base object
        context.deleteObject(entity.getType(), id);
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
