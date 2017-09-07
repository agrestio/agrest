package com.nhl.link.rest.sencha;

import com.nhl.link.rest.LrFeatureProvider;
import org.apache.cayenne.di.Injector;

import javax.ws.rs.core.Feature;

/**@
 * since 2.10
 */
public class SenchaFeatureProvider implements LrFeatureProvider {

    @Override
    public Feature feature(Injector injector) {
        return context -> {
            context.register(SenchaDeletePayloadParser.class);
            return true;
        };
    }
}
