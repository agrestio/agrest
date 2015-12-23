package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.meta.LrEntity;

import java.util.Collections;
import java.util.List;

abstract class SimplePointer implements LrPointer {

    private SimplePointer predecessor;
    private LrEntity<?> entity;

    SimplePointer(SimplePointer predecessor, LrEntity<?> entity) {
        this.predecessor = predecessor;
        this.entity = entity;
    }

    @Override
    public final Class<?> getBaseType() {
        return entity.getType();
    }

    @Override
    public final List<LrPointer> getElements() {
        return Collections.<LrPointer>singletonList(this);
    }

    @Override
    public final Object resolve(PointerContext context, Object baseObject) throws Exception {

        if (context == null) {
            throw new IllegalStateException("Null context in pointer: " + toString());
        }

        return doResolve((CayennePointerContext) context, baseObject);
    }

    @Override
    public final Object resolve(PointerContext context) throws Exception {
        return resolve(context, null);
    }

    protected abstract Object doResolve(CayennePointerContext context, Object baseObject);

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
