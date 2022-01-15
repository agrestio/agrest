package io.agrest.cayenne;

import io.agrest.resolver.NestedDataResolverFactory;
import io.agrest.resolver.RootDataResolverFactory;

/**
 * A collection of root and nested data resolvers for customizing Cayenne entity fetching in Agrest. Resolvers
 * can be installed per-request or per-AgRuntime via {@link io.agrest.meta.AgEntityOverlay}.
 *
 * @see io.agrest.meta.AgEntityOverlay
 * @since 3.4
 */
public class CayenneResolvers {

    /**
     * @since 5.0
     */
    public static RootDataResolverFactory rootViaQuery() {
        return CayenneRootDataResolverBuilder::viaQuery;
    }

    /**
     * Returns a nested resolver that builds a database query using a qualifier from the parent entity. This is the
     * default nested resolver used by the Cayenne backend.
     *
     * @since 5.0
     */
    public static NestedDataResolverFactory nestedViaQueryWithParentExp() {
        return CayenneNestedDataResolverBuilder::viaQueryWithParentExp;
    }


    /**
     * Returns a nested resolver that waits for the parent query to complete, and resolves its objects by building a
     * query from the collection of IDs from the parent result.
     *
     * @since 5.0
     */
    public static NestedDataResolverFactory nestedViaQueryWithParentIds() {
        return CayenneNestedDataResolverBuilder::viaQueryWithParentIds;
    }

    /**
     * Returns a nested resolver that doesn't run its own queries, but instead amends parent node query with prefetch
     * spec, so that the objects can be read efficiently from the parent objects.
     *
     * @since 5.0
     */
    // This will result in a JOINT prefetch on the parent. Note that DISJOINT prefetch is not available as an option,
    // as it is functionally equivalent to "viaQueryWithParentExp", and only complicates implementation without providing
    // a distinct useful alternative
    public static NestedDataResolverFactory nestedViaParentPrefetch() {
        return CayenneNestedDataResolverBuilder::viaParentPrefetch;
    }
}
