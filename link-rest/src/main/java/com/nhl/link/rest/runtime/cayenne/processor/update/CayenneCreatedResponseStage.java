package com.nhl.link.rest.runtime.cayenne.processor.update;

import javax.ws.rs.core.Response;

/**
 * @since 2.7
 */
public class CayenneCreatedResponseStage extends CayenneFillResponseStage {

    public CayenneCreatedResponseStage() {
        super(Response.Status.CREATED);
    }
}
