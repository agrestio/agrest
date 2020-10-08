package io.agrest.cayenne;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.select.CayenneNestedDataResolverBuilder;
import io.agrest.cayenne.processor.select.CayenneRootDataResolverBuilder;
import io.agrest.runtime.AgRuntime;

import javax.ws.rs.core.Configuration;

/**
 * A helper class to build root and nested data resolvers for customizing Cayenne entity fetching in Agrest. Resolvers
 * can be installed per request or per AgRuntime via {@link io.agrest.meta.AgEntityOverlay}.
 *
 * @see io.agrest.meta.AgEntityOverlay
 * @since 3.4
 */
public class CayenneResolvers {

    public static CayenneRootDataResolverBuilder root(Configuration config) {
        return new CayenneRootDataResolverBuilder(persister(config));
    }

    public static CayenneRootDataResolverBuilder root(ICayennePersister persister) {
        return new CayenneRootDataResolverBuilder(persister);
    }

    public static CayenneNestedDataResolverBuilder nested(Configuration config) {
        return new CayenneNestedDataResolverBuilder(persister(config));
    }

    public static CayenneNestedDataResolverBuilder nested(ICayennePersister persister) {
        return new CayenneNestedDataResolverBuilder(persister);
    }

    private static ICayennePersister persister(Configuration config) {
        return AgRuntime.service(ICayennePersister.class, config);
    }
}
