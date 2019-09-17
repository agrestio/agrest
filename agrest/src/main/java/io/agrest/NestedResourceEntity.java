package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgRelationship;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <T>
 * @since 3.4
 */
public class NestedResourceEntity<T> extends ResourceEntity<T> {

    private ResourceEntity<?> parent;
    private AgRelationship incoming;
    private NestedDataResolver<T> resolver;
    private Map<AgObjectId, Object> resultByParent;

    // TODO: Instead of AgRelationship introduce some kind of RERelationship that has references to both parent and child
    public NestedResourceEntity(
            AgEntity<T> agEntity,
            AgEntityOverlay<T> agEntityOverlay,
            ResourceEntity<?> parent,
            AgRelationship incoming) {
        
        super(agEntity, agEntityOverlay);
        this.incoming = incoming;
        this.parent = parent;
        this.resultByParent = new LinkedHashMap<>();
    }

    public ResourceEntity<?> getParent() {
        return parent;
    }

    public AgRelationship getIncoming() {
        return incoming;
    }

    public Object getResult(AgObjectId parentId) {
        return resultByParent.get(parentId);
    }

    public void setToOneResult(AgObjectId parentId, T object) {
        resultByParent.put(parentId, object);
    }

    public void addToManyResult(AgObjectId parentId, T object) {
        ((List<T>) resultByParent.computeIfAbsent(parentId, k -> new ArrayList<>())).add(object);
    }

    public void setToManyResult(AgObjectId parentId, List<T> objects) {
        resultByParent.put(parentId, objects);
    }

    public NestedDataResolver<T> getResolver() {
        return resolver;
    }

    public void setResolver(NestedDataResolver<T> resolver) {
        this.resolver = resolver;
    }

    public void onParentQueryAssembled(SelectContext<?> context) {
        resolver.onParentQueryAssembled(this, context);
    }

    public void onParentDataResolved(Iterable<?> parentData, SelectContext<?> context) {
        resolver.onParentDataResolved(this, parentData, context);
    }
}
