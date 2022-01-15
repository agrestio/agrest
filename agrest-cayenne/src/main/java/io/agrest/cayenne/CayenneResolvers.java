package io.agrest.cayenne;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
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


    /**
     * @since 5.0
     */
    public static CayenneRootDataResolverBuilder root() {
        return new CayenneRootDataResolverBuilder();
    }

    public static CayenneNestedDataResolverBuilder nested(Configuration config) {
        return new CayenneNestedDataResolverBuilder(persister(config), queryAssembler(config));
    }

    /**
     * @since 3.7
     */
    public static CayenneNestedDataResolverBuilder nested(ICayennePersister persister, ICayenneQueryAssembler queryAssembler) {
        return new CayenneNestedDataResolverBuilder(persister, queryAssembler);
    }

    private static ICayennePersister persister(Configuration config) {
        return AgRuntime.service(ICayennePersister.class, config);
    }

    private static ICayenneQueryAssembler queryAssembler(Configuration config) {
        return AgRuntime.service(ICayenneQueryAssembler.class, config);
    }
}
