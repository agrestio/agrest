package io.agrest.runtime.provider.converter;

import io.agrest.protocol.Include;
import io.agrest.runtime.protocol.IIncludeParser;

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
