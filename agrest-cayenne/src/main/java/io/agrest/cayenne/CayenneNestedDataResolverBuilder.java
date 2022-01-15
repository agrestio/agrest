package io.agrest.cayenne;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.processor.select.ViaParentPrefetchResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentExpResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentIdsResolver;
import io.agrest.processor.ProcessingContext;
import io.agrest.resolver.ContextAwareNestedDataResolver;
import io.agrest.resolver.NestedDataResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.PrefetchTreeNode;

class CayenneNestedDataResolverBuilder {

    static NestedDataResolver<?> viaQueryWithParentExp(Class<?> parentType, String relationshipName) {
        return new ContextAwareNestedDataResolver<>(c -> viaQueryWithParentExp(parentType, relationshipName, c));
    }

    private static NestedDataResolver<?> viaQueryWithParentExp(Class<?> parentType, String relationshipName, ProcessingContext<?> context) {
        ICayennePersister persister = context.service(ICayennePersister.class);
        ICayenneQueryAssembler queryAssembler = context.service(ICayenneQueryAssembler.class);

        validateParent(parentType, relationshipName, persister);
        return new ViaQueryWithParentExpResolver(queryAssembler, persister);
    }

    static NestedDataResolver<?> viaQueryWithParentIds(Class<?> parentType, String relationshipName) {
        return new ContextAwareNestedDataResolver<>(c -> viaQueryWithParentIds(parentType, relationshipName, c));
    }

    private static NestedDataResolver<?> viaQueryWithParentIds(Class<?> parentType, String relationshipName, ProcessingContext<?> context) {
        ICayennePersister persister = context.service(ICayennePersister.class);
        ICayenneQueryAssembler queryAssembler = context.service(ICayenneQueryAssembler.class);

        validateParent(parentType, relationshipName, persister);
        return new ViaQueryWithParentIdsResolver(queryAssembler, persister);
    }

    static NestedDataResolver<?> viaParentPrefetch(Class<?> parentType, String relationshipName) {
        return new ContextAwareNestedDataResolver<>(c -> viaParentPrefetch(parentType, relationshipName, c));
    }

    private static NestedDataResolver<?> viaParentPrefetch(Class<?> parentType, String relationshipName, ProcessingContext<?> context) {
        ICayennePersister persister = context.service(ICayennePersister.class);
        validateParent(parentType, relationshipName, persister);
        return new ViaParentPrefetchResolver(PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);
    }

    static void validateParent(Class<?> parentType, String relationshipName, ICayennePersister persister) {

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
