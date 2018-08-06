package com.nhl.link.rest.runtime.provider.converter;

import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;

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
        return null;
    }
}
