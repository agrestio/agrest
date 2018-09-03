package io.agrest.runtime.provider.converter;

import io.agrest.protocol.Exclude;
import io.agrest.runtime.protocol.IExcludeParser;

import javax.ws.rs.ext.ParamConverter;

public class ExcludeConverter implements ParamConverter<Exclude> {

    private final IExcludeParser excludeParser;

    public ExcludeConverter(IExcludeParser excludeParser) {
        this.excludeParser = excludeParser;
    }


    @Override
    public Exclude fromString(String value) {
        return this.excludeParser != null ? this.excludeParser.oneFromString(value) : null;
    }

    @Override
    public String toString(Exclude value) {
        throw new UnsupportedOperationException();
    }
}
