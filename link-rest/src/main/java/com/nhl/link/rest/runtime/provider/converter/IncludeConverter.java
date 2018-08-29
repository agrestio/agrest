package com.nhl.link.rest.runtime.provider.converter;

import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;

import javax.ws.rs.ext.ParamConverter;

public class IncludeConverter implements ParamConverter<Include> {

    private final IIncludeParser includeParser;

    public IncludeConverter(IIncludeParser includeParser) {
        this.includeParser = includeParser;
    }


    @Override
    public Include fromString(String value) {
        return this.includeParser != null ? this.includeParser.oneFromString(value) : null;
    }

    @Override
    public String toString(Include value) {
        throw new UnsupportedOperationException();
    }
}
