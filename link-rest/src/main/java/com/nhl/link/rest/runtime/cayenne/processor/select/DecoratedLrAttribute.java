package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.property.PropertyReader;
import org.apache.cayenne.exp.parser.ASTPath;

class DecoratedLrAttribute implements LrAttribute {

    private LrAttribute delegate;
    private PropertyReader reader;

    public DecoratedLrAttribute(LrAttribute delegate, PropertyReader reader) {
        this.delegate = delegate;
        this.reader = reader;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Class<?> getType() {
        return delegate.getType();
    }

    @Override
    public ASTPath getPathExp() {
        return delegate.getPathExp();
    }

    @Override
    public PropertyReader getPropertyReader() {
        return reader;
    }
}
