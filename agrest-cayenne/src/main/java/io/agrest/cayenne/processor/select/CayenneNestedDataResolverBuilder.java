package io.agrest.cayenne.processor.select;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.NestedDataResolverFactory;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.PrefetchTreeNode;

/**
 * @since 3.4
 */
public class CayenneNestedDataResolverBuilder {

    private final ICayennePersister persister;
    private final ICayenneQueryAssembler queryAssembler;

    public CayenneNestedDataResolverBuilder(ICayennePersister persister, ICayenneQueryAssembler queryAssembler) {
        this.persister = persister;
        this.queryAssembler = queryAssembler;
    }

    /**
     * Returns a nested resolver that builds a database query using a qualifier from the parent entity. This is the
     * default nested resolver used by the Cayenne backend.
     */
    public NestedDataResolverFactory viaQueryWithParentExp() {
        return this::viaQueryWithParentExp;
    }

    /**
     * Returns a nested resolver that waits for the parent query to complete, and resolves its objects by building a
     * query from the collection of IDs from the parent result.
     */
    public NestedDataResolverFactory viaQueryWithParentIds() {
        return this::viaQueryWithParentIds;
    }

    /**
     * Returns a nested resolver that doesn't run its own queries, but instead amends parent node query with prefetch
     * spec, so that the objects can be read efficiently from the parent objects.
     */
    // This will result in a JOINT prefetch on the parent. Note that DISJOINT prefetch is not available as an option,
    // as it is functionally equivalent to "viaQueryWithParentExp", and only complicates implementation without providing
    // a distinct useful alternative
    public NestedDataResolverFactory viaParentPrefetch() {
        return this::viaParentPrefetch;
    }

    protected NestedDataResolver<?> viaQueryWithParentExp(Class<?> parentType, String relationshipName) {
        validateParent(parentType, relationshipName);
        return new ViaQueryWithParentExpResolver(queryAssembler, persister);
    }

    protected NestedDataResolver<?> viaQueryWithParentIds(Class<?> parentType, String relationshipName) {
        validateParent(parentType, relationshipName);
        return new ViaQueryWithParentIdsResolver(queryAssembler, persister);
    }

    public NestedDataResolver<?> viaParentPrefetch(Class<?> parentType, String relationshipName) {
        validateParent(parentType, relationshipName);
        return new ViaParentPrefetchResolver(PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);
    }

    protected void validateParent(Class<?> parentType, String relationshipName) {

        ObjEntity entity = persister.entityResolver().getObjEntity(parentType);

        if (entity == null) {
            throw new IllegalStateException("Entity '" + parentType.getSimpleName()
                    + "' is not mapped in Cayenne, so its relationship '"
                    + relationshipName
                    + "' can't be resolved with a Cayenne resolver");
        }

        if (entity.getRelationship(relationshipName) == null) {
            throw new IllegalStateException("Relationship '" + entity.getName() + "." + relationshipName
                    + "' is not mapped in Cayenne and can't be resolved with a Cayenne resolver");
        }
    }
}
