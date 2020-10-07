package io.agrest;

import io.agrest.unit.AgPojoTester;
import io.agrest.unit.PojoTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class GET_SimpleResponseIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = tester(Resource.class).build();

    @Test
    public void testWrite() {

        tester.target("/simple").get()
                .wasOk()
                .bodyEquals("{\"success\":true,\"message\":\"Hi!\"}");

        tester.target("/simple/2").get()
                .wasOk()
                .bodyEquals("{\"success\":false,\"message\":\"Hi2!\"}");
    }

    @Path("simple")
    public static class Resource {

        @GET
        public SimpleResponse get() {
            return new SimpleResponse(true, "Hi!");
        }

        @GET
        @Path("2")
        public SimpleResponse get2() {
            return new SimpleResponse(false, "Hi2!");
        }
    }
}
