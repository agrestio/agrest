package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.meta.LrEntity;

abstract class SimplePointer implements LrPointer {

    private SimplePointer predecessor;
    private LrEntity<?> entity;

    SimplePointer(SimplePointer predecessor, LrEntity<?> entity) {
        this.predecessor = predecessor;
        this.entity = entity;
    }

    @Override
    public final Class<?> getBaseType() {
        return predecessor == null? entity.getType() : predecessor.getBaseType();
    }

    @Override
    public LrPointer getParent() {
        return predecessor;
    }

    @Override
    public final Object resolve(PointerContext context, Object baseObject) throws Exception {
        return doResolve(context, resolveBase(context, baseObject));
    }

    @Override
    public final Object resolve(PointerContext context) throws Exception {
        return resolve(context, null);
    }

    protected abstract Object doResolve(PointerContext context, Object baseObject);

    @Override
    public void update(PointerContext context, Object baseObject, Object value) throws Exception {
        doUpdate(context, resolveBase(context, baseObject), value);
    }

    @Override
    public void update(PointerContext context, Object value) throws Exception {
        update(context, null, value);
    }

    protected abstract void doUpdate(PointerContext context, Object baseObject, Object value);

    @Override
    public void delete(PointerContext context) throws Exception {
        delete(context, null);
    }

    @Override
    public void delete(PointerContext context, Object baseObject) throws Exception {
        doDelete(context, resolveBase(context, baseObject));
    }

    protected abstract void doDelete(PointerContext context, Object baseObject);

    private Object resolveBase(PointerContext context, Object baseObject) throws Exception {

        if (context == null) {
            throw new IllegalStateException("Null context in pointer: " + toString());
        }

        if (predecessor != null) {
            baseObject = predecessor.resolve(context, baseObject);
        }
        return baseObject;
    }

    /**
     * @return Escaped string representation of this pointer
     */
    protected abstract String encodeToString();

    @Override
    public final String toString() {
        return predecessor == null?
                this.encodeToString() :
                Pointers.buildPath(
                        predecessor.encodeToString(),
                        this.encodeToString()
                );
    }
}
