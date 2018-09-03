package io.agrest.runtime.provider.converter;

import io.agrest.protocol.MapBy;
import io.agrest.runtime.protocol.IMapByParser;

import javax.ws.rs.ext.ParamConverter;

public class MapByConverter implements ParamConverter<MapBy> {

    private final IMapByParser mapByParser;

    public MapByConverter(IMapByParser mapByParser) {
        this.mapByParser = mapByParser;
    }

    @Override
    public MapBy fromString(String value) {
        return this.mapByParser != null ? this.mapByParser.fromString(value) : null;
    }

    @Override
    public String toString(MapBy value) {
        throw new UnsupportedOperationException();
    }
}
