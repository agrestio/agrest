package io.agrest.cayenne;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.processor.select.ViaParentPrefetchResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentExpResolver;
import io.agrest.cayenne.processor.select.ViaQueryWithParentIdsResolver;
import io.agrest.processor.ProcessingContext;
import io.agrest.resolver.ContextAwareRelatedDataResolver;
import io.agrest.resolver.RelatedDataResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.PrefetchTreeNode;

class CayenneRelatedDataResolverBuilder {

    static RelatedDataResolver<?> viaQueryWithParentExp(Class<?> parentType, String relationshipName) {
        return new ContextAwareRelatedDataResolver<>(c -> viaQueryWithParentExp(parentType, relationshipName, c));
    }

    private static RelatedDataResolver<?> viaQueryWithParentExp(Class<?> parentType, String relationshipName, ProcessingContext<?> context) {
        ICayennePersister persister = context.service(ICayennePersister.class);
        ICayenneQueryAssembler queryAssembler = context.service(ICayenneQueryAssembler.class);

        validateParent(parentType, relationshipName, persister);
        return new ViaQueryWithParentExpResolver(queryAssembler, persister);
    }

    static RelatedDataResolver<?> viaQueryWithParentIds(Class<?> parentType, String relationshipName) {
        return new ContextAwareRelatedDataResolver<>(c -> viaQueryWithParentIds(parentType, relationshipName, c));
    }

    private static RelatedDataResolver<?> viaQueryWithParentIds(Class<?> parentType, String relationshipName, ProcessingContext<?> context) {
        ICayennePersister persister = context.service(ICayennePersister.class);
        ICayenneQueryAssembler queryAssembler = context.service(ICayenneQueryAssembler.class);

        validateParent(parentType, relationshipName, persister);
        return new ViaQueryWithParentIdsResolver(queryAssembler, persister);
    }

    static RelatedDataResolver<?> viaParentPrefetch(Class<?> parentType, String relationshipName) {
        return new ContextAwareRelatedDataResolver<>(c -> viaParentPrefetch(parentType, relationshipName, c));
    }

    private static RelatedDataResolver<?> viaParentPrefetch(Class<?> parentType, String relationshipName, ProcessingContext<?> context) {
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
