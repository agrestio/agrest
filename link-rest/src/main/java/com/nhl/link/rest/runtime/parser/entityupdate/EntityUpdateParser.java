package com.nhl.link.rest.runtime.parser.entityupdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Inject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @since 1.20
 */
public class EntityUpdateParser implements IEntityUpdateParser {

    protected IJacksonService jacksonService;

    private EntityUpdateJsonTraverser entityUpdateJsonTraverser;

    public EntityUpdateParser(@Inject IRelationshipMapper relationshipMapper,
                              @Inject IJacksonService jacksonService,
                              @Inject IJsonValueConverterFactory converterFactory) {
        this.jacksonService = jacksonService;
        this.entityUpdateJsonTraverser = new EntityUpdateJsonTraverser(relationshipMapper, converterFactory);
    }

    @Override
    public <T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, InputStream entityStream) {
        JsonNode node = jacksonService.parseJson(entityStream);
        return parse(entity, node);
    }

    @Override
    public <T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, String entityData) {
        JsonNode node = jacksonService.parseJson(entityData);
        return parse(entity, node);
    }

    protected <T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, JsonNode json) {
        UpdateVisitor<T> visitor = updateVisitor(entity);
        entityJsonTraverser().traverse(entity, json, visitor);
        return visitor.getUpdates();
    }

    protected EntityUpdateJsonTraverser entityJsonTraverser() {
        return entityUpdateJsonTraverser;
    }

    protected <T> UpdateVisitor<T> updateVisitor(LrEntity<T> entity) {
        return new UpdateVisitor<>(entity);
    }

    protected static class UpdateVisitor<T> implements EntityUpdateJsonVisitor {

        private LrEntity<T> entity;
        private Collection<EntityUpdate<T>> updates;

        private EntityUpdate<T> currentUpdate;

        protected UpdateVisitor(LrEntity<T> entity) {
            this.entity = entity;
            this.updates = new ArrayList<>();
        }

        @Override
        public void beginObject() {
            currentUpdate = new EntityUpdate<>(entity);
        }

        @Override
        public void visitId(String name, Object value) {
            currentUpdate.getOrCreateId().put(name, value);
        }

        @Override
        public void visitId(Map<String, Object> value) {
            currentUpdate.getOrCreateId().putAll(value);
        }

        @Override
        public void visitAttribute(String name, Object value) {
            currentUpdate.getValues().put(name, value);
        }

        @Override
        public void visitRelationship(String name, Object relatedId) {
		    currentUpdate.addRelatedId(name, relatedId);
        }

        @Override
        public void endObject() {
            updates.add(currentUpdate);
            currentUpdate = null;
        }

        Collection<EntityUpdate<T>> getUpdates() {
            return updates;
        }
    }
}
