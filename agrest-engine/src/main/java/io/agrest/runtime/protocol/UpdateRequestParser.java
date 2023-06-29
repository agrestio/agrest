package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.EntityUpdate;
import io.agrest.converter.jsonvalue.JsonValueConverters;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 5.0
 */
public class UpdateRequestParser implements IUpdateRequestParser {

    private static final int MAX_DEPTH = 100;

    private final IJacksonService jacksonService;
    private final JsonValueConverters converters;
    private final Map<String, EntityUpdateParser> perEntityParsers;

    public UpdateRequestParser(@Inject IJacksonService jacksonService, @Inject JsonValueConverters converters) {
        this.jacksonService = jacksonService;
        this.converters = converters;
        this.perEntityParsers = new ConcurrentHashMap<>();
    }

    @Override
    public <T> List<EntityUpdate<T>> parse(AgEntity<T> entity, InputStream entityStream) {
        JsonNode json = jacksonService.parseJson(entityStream);

        // TODO: once recursive EntityUpdates are to be used for real in Agrest, replace the hardcoded value
        //  of max depth with a value specified in request
        return getParser(entity).parse(json, MAX_DEPTH);
    }

    @Override
    public <T> List<EntityUpdate<T>> parse(AgEntity<T> entity, String entityData) {
        JsonNode json = jacksonService.parseJson(entityData);

        // TODO: once recursive EntityUpdates are to be used for real in Agrest, replace the hardcoded value
        //  of max depth with a value specified in request
        return getParser(entity).parse(json, MAX_DEPTH);
    }

    protected <T> EntityUpdateParser<T> getParser(AgEntity<T> entity) {
        return perEntityParsers
                .computeIfAbsent(entity.getName(), n -> new EntityUpdateParser<>(entity, this, converters));
    }
}
