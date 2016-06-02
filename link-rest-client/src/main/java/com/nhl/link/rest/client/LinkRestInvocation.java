package com.nhl.link.rest.client;

import javax.ws.rs.core.Response;

@FunctionalInterface
public interface LinkRestInvocation {

    Response invoke();
}
