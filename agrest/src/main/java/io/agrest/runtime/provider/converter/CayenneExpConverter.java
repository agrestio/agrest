package io.agrest.runtime.provider.converter;

import io.agrest.protocol.CayenneExp;
import io.agrest.runtime.protocol.ICayenneExpParser;

import javax.ws.rs.ext.ParamConverter;

public class CayenneExpConverter implements ParamConverter<CayenneExp> {

    private final ICayenneExpParser cayenneExpParser;

    public CayenneExpConverter(ICayenneExpParser cayenneExpParser) {
        this.cayenneExpParser = cayenneExpParser;
    }

    @Override
    public CayenneExp fromString(String value) {
        return this.cayenneExpParser != null ? this.cayenneExpParser.fromString(value) : null;
    }

    @Override
    public String toString(CayenneExp value) {
        throw new UnsupportedOperationException();
    }
}
