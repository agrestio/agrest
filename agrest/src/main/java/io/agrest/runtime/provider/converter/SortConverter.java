package io.agrest.runtime.provider.converter;

import io.agrest.protocol.Sort;
import io.agrest.runtime.protocol.ISortParser;

import javax.ws.rs.ext.ParamConverter;

public class SortConverter implements ParamConverter<Sort> {

    private final ISortParser sortParser;

    public SortConverter(ISortParser sortParser) {
        this.sortParser = sortParser;
    }

    @Override
    public Sort fromString(String value) {
        return this.sortParser != null ? this.sortParser.fromString(value) : null;
    }

    @Override
    public String toString(Sort value) {
        throw new UnsupportedOperationException();
    }
}
