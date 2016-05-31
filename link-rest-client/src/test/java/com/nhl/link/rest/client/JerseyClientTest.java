package com.nhl.link.rest.client;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.Application;

public abstract class JerseyClientTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig();
    }
}
