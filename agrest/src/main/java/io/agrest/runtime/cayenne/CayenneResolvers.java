package io.agrest.runtime.cayenne;

import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.cayenne.processor.select.CayenneQueryAssembler;
import io.agrest.runtime.cayenne.processor.select.ViaParentPrefetchResolver;
import io.agrest.runtime.cayenne.processor.select.ViaQueryResolver;
import io.agrest.runtime.cayenne.processor.select.ViaQueryWithParentIdsResolver;
import io.agrest.runtime.cayenne.processor.select.ViaQueryWithParentExpResolver;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.query.PrefetchTreeNode;

import javax.ws.rs.core.Configuration;

/**
 * Provides root and nested data resolvers that can be used with Cayenne entities in Agrest.
 *
 * @since 3.4
 */
public class CayenneResolvers {

    public static ICayennePersister persister(Configuration config) {
        return AgRuntime.service(ICayennePersister.class, config);
    }

    public static <T extends DataObject> RootDataResolver<T> rootViaQuery(Configuration config) {
        return rootViaQuery(persister(config));
    }

    public static <T extends DataObject> RootDataResolver<T> rootViaQuery(ICayennePersister persister) {
        return (RootDataResolver<T>) new ViaQueryResolver(
                new CayenneQueryAssembler(persister.entityResolver()),
                persister);
    }

    public static <T extends DataObject> NestedDataResolver<T> nestedViaDisjointParentPrefetch() {
        return (NestedDataResolver<T>) new ViaParentPrefetchResolver(PrefetchTreeNode.DISJOINT_PREFETCH_SEMANTICS);
    }

    public static <T extends DataObject> NestedDataResolver<T> nestedViaJointParentPrefetch() {
        return (NestedDataResolver<T>) new ViaParentPrefetchResolver(PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);
    }

    public static <T extends DataObject> NestedDataResolver<T> nestedViaQueryWithParentExp(Configuration config) {
        return nestedViaQueryWithParentExp(persister(config));
    }

    public static <T extends DataObject> NestedDataResolver<T> nestedViaQueryWithParentExp(ICayennePersister persister) {
        return (NestedDataResolver<T>) new ViaQueryWithParentExpResolver(
                new CayenneQueryAssembler(persister.entityResolver()),
                persister);
    }

    public static <T extends DataObject> NestedDataResolver<T> nestedViaQueryWithParentIds(Configuration config) {
        return nestedViaQueryWithParentIds(persister(config));
    }

    public static <T extends DataObject> NestedDataResolver<T> nestedViaQueryWithParentIds(ICayennePersister persister) {
        return (NestedDataResolver<T>) new ViaQueryWithParentIdsResolver(
                new CayenneQueryAssembler(persister.entityResolver()),
                persister);
    }
}
