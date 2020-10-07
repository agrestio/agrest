package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E10;
import io.agrest.cayenne.cayenne.main.E11;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.constraints.Constraint;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_ConstraintsIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E4.class, E10.class, E11.class)
            .build();

    @Test
    public void testImplicit() {

        tester.e4().insertColumns("id", "c_varchar", "c_int").values(1, "xxx", 5).exec();

        tester.target("/e4/limit_attributes")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":1,\"cInt\":5}");
    }

    @Test
    public void testExplicit() {

        tester.e4().insertColumns("id", "c_varchar", "c_int").values(1, "xxx", 5).exec();

        tester.target("/e4/limit_attributes")
                .queryParam("include", E4.C_BOOLEAN.getName())
                .queryParam("include", E4.C_INT.getName())
                .get()
                .wasOk().bodyEquals(1, "{\"cInt\":5}");
    }

    @Test
    public void testAnnotated() {

        tester.e10().insertColumns("id", "c_varchar", "c_int", "c_boolean", "c_date")
                .values(1, "xxx", 5, true, "2014-01-02").exec();

        tester.target("/e10")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"cBoolean\":true,\"cInt\":5}");
    }

    @Test
    public void testAnnotated_Relationship() {

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
        @Path("e4/limit_attributes")
        public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {
            return Ag.select(E4.class, config).uri(uriInfo)
                    .constraint(Constraint.idOnly(E4.class).attributes(E4.C_INT.getName()))
                    .get();
        }

        @GET
        @Path("e10")
        public DataResponse<E10> get(@Context UriInfo uriInfo) {
            return Ag.select(E10.class, config).uri(uriInfo).get();
        }
    }
}
