package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.EntityUpdate;
import io.agrest.converter.jsonvalue.JsonValueConverters;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 5.0
 */
public class UpdateRequestParser implements IUpdateRequestParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRequestParser.class);

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
        return parse(entity, json);
    }

    @Override
    public <T> List<EntityUpdate<T>> parse(AgEntity<T> entity, String entityData) {
        JsonNode json = jacksonService.parseJson(entityData);
        return parse(entity, json);
    }

    protected <T> List<EntityUpdate<T>> parse(AgEntity<T> entity, JsonNode json) {

        // TODO: once recursive EntityUpdates are to be used for real in Agrest, replace the hardcoded value
        //  of max depth with a value specified in request

        return perEntityParsers
                .computeIfAbsent(entity.getName(), n -> new EntityUpdateParser<>(entity, this, converters))
                .parse(json, MAX_DEPTH);
    }

    protected <T> List<EntityUpdate<T>> parse(AgRelationship relationship, JsonNode json, int remainingDepth) {

        if (remainingDepth == 0) {
            LOGGER.info("Truncated updates for relationship '{}' pointing to '{}', as it exceeds the max allowed depth",
                    relationship.getName(),
                    relationship.getTargetEntity().getName());

            return Collections.emptyList();
        }

        AgEntity entity = relationship.getTargetEntity();

        return perEntityParsers
                .computeIfAbsent(entity.getName(), n -> new EntityUpdateParser<>(entity, this, converters))
                .parse(json, remainingDepth);
    }

}
