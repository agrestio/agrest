package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.property.PropertyReader;
import org.apache.cayenne.exp.parser.ASTPath;

class CountAttribute implements LrAttribute {

    private static final CountAttribute instance = new CountAttribute();

    public static CountAttribute instance() {
        return instance;
    }

    @Override
    public String getName() {
        return "count()";
    }

    @Override
    public Class<?> getType() {
        return Long.class;
    }

    @Override
    public ASTPath getPathExp() {
        return null;
    }

    @Override
    public PropertyReader getPropertyReader() {
        return null;
    }
}
