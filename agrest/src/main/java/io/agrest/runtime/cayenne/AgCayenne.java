package io.agrest.runtime.cayenne;

import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.cayenne.processor.select.CayenneQueryAssembler;
import io.agrest.runtime.cayenne.processor.select.ViaParentPrefetchResolver;
import io.agrest.runtime.cayenne.processor.select.ViaQueryResolver;
import io.agrest.runtime.cayenne.processor.select.ViaQueryWithParentIdsResolver;
import io.agrest.runtime.cayenne.processor.select.ViaQueryWithParentQualifierResolver;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.query.PrefetchTreeNode;

import javax.ws.rs.core.Configuration;

/**
 * Provides access to Cayenne backend services within Agrest / JAX RS runtume.
 *
 * @since 3.4
 */
public class AgCayenne {

    public static ICayennePersister persister(Configuration config) {
        return AgRuntime.service(ICayennePersister.class, config);
    }

    public static <T extends DataObject> RootDataResolver<T> rootResolverViaQuery(Configuration config) {
        return rootResolverViaQuery(persister(config));
    }

    public static <T extends DataObject> RootDataResolver<T> rootResolverViaQuery(ICayennePersister persister) {
        return (RootDataResolver<T>) new ViaQueryResolver(
                new CayenneQueryAssembler(persister.entityResolver()),
                persister);
    }

    public static <T extends DataObject> NestedDataResolver<T> resolverViaDisjointParentPrefetch() {
        return (NestedDataResolver<T>) new ViaParentPrefetchResolver(PrefetchTreeNode.DISJOINT_PREFETCH_SEMANTICS);
    }

    public static <T extends DataObject> NestedDataResolver<T> resolverViaJointParentPrefetch() {
        return (NestedDataResolver<T>) new ViaParentPrefetchResolver(PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);
    }

    public static <T extends DataObject> NestedDataResolver<T> resolverViaQueryWithParentQualifier(Configuration config) {
        return resolverViaQueryWithParentQualifier(persister(config));
    }

    public static <T extends DataObject> NestedDataResolver<T> resolverViaQueryWithParentQualifier(ICayennePersister persister) {
        return (NestedDataResolver<T>) new ViaQueryWithParentQualifierResolver(
                new CayenneQueryAssembler(persister.entityResolver()),
                persister);
    }

    public static <T extends DataObject> NestedDataResolver<T> resolverViaQueryWithParentIds(Configuration config) {
        return resolverViaQueryWithParentIds(persister(config));
    }

    public static <T extends DataObject> NestedDataResolver<T> resolverViaQueryWithParentIds(ICayennePersister persister) {
        return (NestedDataResolver<T>) new ViaQueryWithParentIdsResolver(
                new CayenneQueryAssembler(persister.entityResolver()),
                persister);
    }
}
