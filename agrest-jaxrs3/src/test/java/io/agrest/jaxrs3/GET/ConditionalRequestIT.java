package io.agrest.jaxrs3.GET;

import io.agrest.DataResponse;
import io.agrest.HttpStatus;
import io.agrest.SelectStage;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.jaxrs3.junit.AgPojoTester;
import io.agrest.jaxrs3.junit.PojoTest;
import io.agrest.jaxrs3.junit.pojo.P1;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.select.SelectContext;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class ConditionalRequestIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void noRequestEtag() {

        P1 o1 = new P1();
        o1.setName("n1");
        tester.p1().put(1, o1);

        Response r = tester.internalTarget()
                .request()
                .get();

        JettyTester
                .assertOk(r)
                .assertHeader("ETag", "my-tag")
                .assertContent("{\"data\":[{\"name\":\"n1\"}],\"total\":1}");
    }

    @Test
    public void eTag() {

        P1 o1 = new P1();
        o1.setName("n1");
        tester.p1().put(1, o1);

        Response r = tester.internalTarget()
                .request()
                .header("If-None-Match", "my-tag")
                .get();

        JettyTester
                .assertStatus(r, HttpStatus.NOT_MODIFIED)
                .assertHeader("ETag", "my-tag")
                .assertContent("");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        public DataResponse<P1> conditional(@HeaderParam("If-None-Match") String eTag) {
            return AgJaxrs
                    .select(P1.class, config)
                    .routingStage(SelectStage.APPLY_SERVER_PARAMS, c -> stopIfMatch(c, eTag))
                    .stage(SelectStage.ENCODE, this::cacheControl)
                    .get();
        }

        ProcessorOutcome stopIfMatch(SelectContext<?> context, String eTag) {
            if (!"my-tag".equals(eTag)) {
                return ProcessorOutcome.CONTINUE;
            }
            
            context.setResponseStatus(HttpStatus.NOT_MODIFIED);
            context.addResponseHeader("etag", eTag);
            return ProcessorOutcome.STOP;
        }

        void cacheControl(SelectContext<?> context) {
            context.addResponseHeader("etag", "my-tag");
        }
    }
}
