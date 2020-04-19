package io.agrest.cayenne.processor.update;

import javax.ws.rs.core.Response;

/**
 * @since 2.7
 */
public class CayenneOkResponseStage extends CayenneFillResponseStage {

    public CayenneOkResponseStage() {
        super(Response.Status.OK);
    }
}
