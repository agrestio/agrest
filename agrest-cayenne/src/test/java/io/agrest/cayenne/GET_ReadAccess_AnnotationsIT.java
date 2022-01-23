package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E10;
import io.agrest.cayenne.cayenne.main.E11;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_ReadAccess_AnnotationsIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E10.class, E11.class)
            .build();

    @Test
    public void testAnnotationReadableFlag_Attributes() {

        tester.e10().insertColumns("id", "c_varchar", "c_int", "c_boolean", "c_date")
                .values(1, "xxx", 5, true, "2014-01-02").exec();

        tester.target("/e10")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"cBoolean\":true,\"cInt\":5}");
    }

    @Test
    public void testAnnotationReadableFlag_Relationship() {

        tester.e10().insertColumns("id", "c_varchar", "c_int", "c_boolean", "c_date")
                .values(1, "xxx", 5, true, "2014-01-02").exec();

        tester.e11().insertColumns("id", "e10_id", "address", "name")
                .values(15, 1, "aaa", "nnn").exec();

        tester.target("/e10").queryParam("include", E10.E11S.getName())
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"cBoolean\":true,\"cInt\":5,\"e11s\":[{\"address\":\"aaa\"}]}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e10")
        public DataResponse<E10> get(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E10.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }
}
