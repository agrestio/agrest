package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.property.PropertyReader;

class DecoratedLrRelationship implements LrRelationship {

    private LrRelationship delegate;
    private PropertyReader reader;

    public DecoratedLrRelationship(LrRelationship delegate, PropertyReader reader) {
        this.delegate = delegate;
        this.reader = reader;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public LrEntity<?> getTargetEntity() {
        return delegate.getTargetEntity();
    }

    @Override
    public boolean isToMany() {
        return delegate.isToMany();
    }

    @Override
    public PropertyReader getPropertyReader() {
        return reader;
    }
}
