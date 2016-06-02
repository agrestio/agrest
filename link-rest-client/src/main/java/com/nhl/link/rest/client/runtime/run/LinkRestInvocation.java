package com.nhl.link.rest.client.runtime.run;

import javax.ws.rs.core.Response;

/**
 * @since 2.0
 */
@FunctionalInterface
public interface LinkRestInvocation {

    Response invoke();
}
