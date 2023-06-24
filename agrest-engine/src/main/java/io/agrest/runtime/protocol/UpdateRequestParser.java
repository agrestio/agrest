package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.EntityUpdate;
import io.agrest.converter.jsonvalue.JsonValueConverters;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 5.0
 */
public class UpdateRequestParser implements IUpdateRequestParser {

    private final IJacksonService jacksonService;
    private final JsonValueConverters converters;
    private final Map<String, EntityUpdateParser> perEntityParsers;

    public UpdateRequestParser(@Inject IJacksonService jacksonService, @Inject JsonValueConverters converters) {
        this.jacksonService = jacksonService;
        this.converters = converters;
        this.perEntityParsers = new ConcurrentHashMap<>();
    }

    @Override
    public <T> Collection<EntityUpdate<T>> parse(AgEntity<T> entity, InputStream entityStream) {
        JsonNode json = jacksonService.parseJson(entityStream);
        return parse(entity, json);
    }

    @Override
    public <T> Collection<EntityUpdate<T>> parse(AgEntity<T> entity, String entityData) {
        JsonNode json = jacksonService.parseJson(entityData);
        return parse(entity, json);
    }

    protected <T> Collection<EntityUpdate<T>> parse(AgEntity<T> entity, JsonNode json) {
        return perEntityParsers
                .computeIfAbsent(entity.getName(), n -> new EntityUpdateParser<>(entity, converters))
                .parse(json);
    }

}
