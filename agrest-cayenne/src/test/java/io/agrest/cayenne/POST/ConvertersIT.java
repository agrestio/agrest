package io.agrest.cayenne.POST;


import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E16;
import io.agrest.cayenne.cayenne.main.E19;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class ConvertersIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E4.class, E16.class, E19.class)
            .build();

    @Test
    public void string() {

        tester.target("/e4")
                .queryParam("include", "cVarchar")
                .post("{\"cVarchar\":\"zzz\"}")
                .wasCreated()
                .bodyEquals(1, "{\"cVarchar\":\"zzz\"}");

        tester.e4().matcher().assertOneMatch();
        tester.e4().matcher().eq("c_varchar", "zzz").assertOneMatch();
    }

    @Test
    public void _boolean() {

        tester.target("/e4")
                .queryParam("include", "cBoolean")
                .post("{\"cBoolean\":true}")
                .wasCreated()
                .bodyEquals(1, "{\"cBoolean\":true}");

        tester.e4().matcher().assertOneMatch();
        tester.e4().matcher().eq("c_boolean", "true").assertOneMatch();
    }

    @Test
    public void sqlDate() {
        tester.target("e16")
                .queryParam("include", "cDate")
                .post("{\"cDate\":\"2015-03-14\"}")
                .wasCreated()
                .bodyEquals(1, "{\"cDate\":\"2015-03-14\"}");
    }

    @Test
    public void sqlTime() {
        tester.target("e16")
                .queryParam("include", "cTime")
                .post("{\"cTime\":\"19:00:00\"}")
                .wasCreated()
                // TODO: why is time returned back without a "T" prefix?
                .bodyEquals(1, "{\"cTime\":\"19:00:00\"}");
    }

    @Test
    public void sqlTimestamp() {
        tester.target("e16")
                .queryParam("include", "cTimestamp")
                .post("{\"cTimestamp\":\"2015-03-14T19:00:00.000\"}")
                .wasCreated()
                // TODO: why is time returned back without a "T" prefix?
                .bodyEquals(1, "{\"cTimestamp\":\"2015-03-14T19:00:00\"}");
    }

    @Test
    public void byteArray() {

        String base64Encoded = "c29tZVZhbHVlMTIz"; // someValue123

        tester.target("/e19")
                .queryParam("include", E19.GUID.getName())
                .post("{\"guid\":\"" + base64Encoded + "\"}")
                .wasCreated()
                .bodyEquals(1, "{\"guid\":\"" + base64Encoded + "\"}");
    }

    @Test
    public void bigDecimal() {

        tester.target("/e19")
                .queryParam("include", E19.BIG_DECIMAL.getName())
                .post("{\"bigDecimal\":123456789.12}")
                .wasCreated()
                .bodyEquals(1, "{\"bigDecimal\":123456789.12}");
        tester.e19().matcher().eq("big_decimal", new BigDecimal("123456789.12")).assertOneMatch();
    }

    @Test
    public void bigInteger() {

        tester.target("/e19")
                .queryParam("include", E19.BIG_INTEGER.getName())
                .post("{\"bigInteger\":123456789}")
                .wasCreated()
                .bodyEquals(1, "{\"bigInteger\":123456789}");
        tester.e19().matcher().eq("big_integer", new BigInteger("123456789")).assertOneMatch();
    }

    @Test
    public void _byte() {

        tester.target("/e19")
                .queryParam("include", E19.BYTE_OBJECT.getName(), E19.BYTE_PRIMITIVE.getName())
                .post("{\"byteObject\":1,\"bytePrimitive\":2}")
                .wasCreated()
                .bodyEquals(1, "{\"byteObject\":1,\"bytePrimitive\":2}");
        tester.e19().matcher().eq("byte_object", 1).andEq("byte_primitive", 2).assertOneMatch();
    }

    @Test
    public void _short() {

        tester.target("/e19")
                .queryParam("include", E19.SHORT_OBJECT.getName(), E19.SHORT_PRIMITIVE.getName())
                .post("{\"shortObject\":1,\"shortPrimitive\":2}")
                .wasCreated()
                .bodyEquals(1, "{\"shortObject\":1,\"shortPrimitive\":2}");
        tester.e19().matcher().eq("short_object", 1).andEq("short_primitive", 2).assertOneMatch();
    }

    @Test
    public void _float() {
        tester.target("/e19/float")
                .queryParam("include", "floatObject", "floatPrimitive")
                .post("{\"floatObject\":1.0,\"floatPrimitive\":2.0}")
                .wasCreated()
                .bodyEquals(1, "{\"floatObject\":1.0,\"floatPrimitive\":2.0}");
        tester.e19().matcher().eq("float_object", 1.0).andEq("float_primitive", 2.0).assertOneMatch();
    }

    @Test
    public void float_FromInt() {
        tester.target("/e19/float")
                .queryParam("include", "floatObject", "floatPrimitive")
                .post("{\"floatObject\":1,\"floatPrimitive\":2}")
                .wasCreated()
                .bodyEquals(1, "{\"floatObject\":1.0,\"floatPrimitive\":2.0}");
        tester.e19().matcher().eq("float_object", 1.0).andEq("float_primitive", 2.0).assertOneMatch();
    }

    @Test
    public void _double() {
        tester.target("/e19/double").post("{\"doubleObject\":1.0,\"doublePrimitive\":2.0}").wasCreated();
        tester.e19().matcher().eq("double_object", 1.0).andEq("double_primitive", 2.0).assertOneMatch();
    }

    @Test
    public void double_FromInt() {
        tester.target("/e19/double").post("{\"doubleObject\":1,\"doublePrimitive\":2}").wasCreated();
        tester.e19().matcher().eq("double_object", 1.0).andEq("double_primitive", 2.0).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e4")
        public DataResponse<E4> createE4(@QueryParam("include") List<String> includes, String requestBody) {
            return AgJaxrs.create(E4.class, config)
                    .request(AgJaxrs.request(config).addIncludes(includes).build())
                    .syncAndSelect(requestBody);
        }

        @POST
        @Path("e16")
        public DataResponse<E16> createE16(@QueryParam("include") List<String> includes, String requestBody) {
            return AgJaxrs.create(E16.class, config)
                    .request(AgJaxrs.request(config).addIncludes(includes).build())
                    .syncAndSelect(requestBody);
        }

        @POST
        @Path("e19")
        public DataResponse<E19> createE19(@Context UriInfo uriInfo, String data) {
            return AgJaxrs.create(E19.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(data);
        }

        @POST
        @Path("e19/float")
        public DataResponse<E19> createE19_FloatAttribute(@Context UriInfo uriInfo, String data) {
            return AgJaxrs.create(E19.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(data);
        }

        @POST
        @Path("e19/double")
        public SimpleResponse create_E19_DoubleAttribute(String entityData) {
            return AgJaxrs.create(E19.class, config).sync(entityData);
        }
    }
}
