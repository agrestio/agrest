package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E19;
import io.agrest.cayenne.cayenne.main.E28;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class GET_ConvertersIT extends MainDbTest  {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E4.class, E19.class, E28.class)
            .build();

    @Test
    public void testTimestampAsUtilDate() {

        LocalDateTime dateTime = LocalDateTime.of(2012, 2, 3, 11, 1, 2);
        tester.e4().insertColumns("c_timestamp").values(dateTime).exec();

        tester.target("/e4").queryParam("include", E4.C_TIMESTAMP.getName()).get()
                .wasOk().bodyEquals(1, "{\"cTimestamp\":\"2012-02-03T11:01:02\"}");
    }

    @Test
    public void testDateAsUtilDate() {

        LocalDate date = LocalDate.of(2012, 2, 3);
        tester.e4().insertColumns("c_date").values(date).exec();

        tester.target("/e4").queryParam("include", E4.C_DATE.getName())
                .get()
                .wasOk()
                .bodyEquals(1, "{\"cDate\":\"2012-02-03T00:00:00\"}");
    }

    @Test
    public void testTimeAsUtilDate() {
        LocalTime lt = LocalTime.of(14, 0, 1);
        tester.e4().insertColumns("c_time").values(lt).exec();
        tester.target("/e4").queryParam("include", E4.C_TIME.getName())
                .get()
                .wasOk()
                .bodyEquals(1, "{\"cTime\":\"1970-01-01T14:00:01\"}");
    }

    @Test
    public void testSqlTimestamp() {

        LocalDateTime ts = LocalDateTime.of(2012, 2, 3, 11, 1, 2);
        tester.e19().insertColumns("id", "c_timestamp")
                .values(35, ts).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.C_TIMESTAMP.getName())
                .get().wasOk()
                .bodyEquals(1, "{\"cTimestamp\":\"2012-02-03T11:01:02\"}");
    }

    @Test
    public void testSqlDate() {

        LocalDate date = LocalDate.of(2012, 2, 3);
        tester.e19().insertColumns("id", "c_date")
                .values(35, date).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.C_DATE.getName())
                .get().wasOk()
                .bodyEquals(1, "{\"cDate\":\"2012-02-03\"}");
    }

    @Test
    public void testSqlTime() {
        LocalTime t = LocalTime.of(14, 0, 1);
        tester.e19().insertColumns("id", "c_time")
                .values(35, t).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.C_TIME.getName())
                .get().wasOk()
                .bodyEquals(1, "{\"cTime\":\"14:00:01\"}");
    }

    @Test
    public void testByteArray() {

        tester.e19().insertColumns("id", "guid").values(35, "someValue123".getBytes(StandardCharsets.UTF_8)).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.GUID.getName())
                .get().wasOk().bodyEquals(1, "{\"guid\":\"c29tZVZhbHVlMTIz\"}");
    }

    @Test
    public void testBoolean() {

        tester.e19().insertColumns("id", "boolean_object", "boolean_primitive")
                .values(35, true, true).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.BOOLEAN_OBJECT.getName(), E19.BOOLEAN_PRIMITIVE.getName())
                .get().wasOk().bodyEquals(1, "{\"booleanObject\":true,\"booleanPrimitive\":true}");
    }

    @Test
    public void testByte() {

        tester.e19().insertColumns("id", "byte_object", "byte_primitive")
                .values(35, 1, 2).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.BYTE_OBJECT.getName(), E19.BYTE_PRIMITIVE.getName())
                .get().wasOk().bodyEquals(1, "{\"byteObject\":1,\"bytePrimitive\":2}");
    }


    @Test
    public void testShort() {

        tester.e19().insertColumns("id", "short_object", "short_primitive")
                .values(35, 1, 2).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.SHORT_OBJECT.getName(), E19.SHORT_PRIMITIVE.getName())
                .get().wasOk().bodyEquals(1, "{\"shortObject\":1,\"shortPrimitive\":2}");
    }


    @Test
    public void testLong() {

        tester.e19().insertColumns("id", "long_object", "long_primitive")
                .values(35, 13434234234L, 13434234235L).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.LONG_OBJECT.getName(), E19.LONG_PRIMITIVE.getName())
                .get().wasOk().bodyEquals(1, "{\"longObject\":13434234234,\"longPrimitive\":13434234235}");
    }

    @Test
    public void testBigInteger() {

        tester.e19().insertColumns("id", "big_integer")
                .values(35, new BigInteger("1234567890")).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.BIG_INTEGER.getName())
                .get().wasOk().bodyEquals(1, "{\"bigInteger\":1234567890}");
    }

    @Test
    public void testBigDecimal() {

        tester.e19().insertColumns("id", "big_decimal")
                .values(35, new BigDecimal("123456789.12")).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.BIG_DECIMAL.getName())
                .get().wasOk().bodyEquals(1, "{\"bigDecimal\":123456789.12}");
    }

    @Test
    public void testJson() {

        tester.e28().insertColumns("id", "json")
                .values(35, "[1,2]")
                .values(36, "{\"a\":1}")
                .values(37, "{}")
                .values(38, "5")
                .values(39, null)
                .exec();

        tester.target("/e28")
                .queryParam("include", E28.JSON.getName())
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(5, "{\"json\":[1,2]}", "{\"json\":{\"a\":1}}", "{\"json\":{}}", "{\"json\":5}", "{\"json\":null}");
    }

    @Test
    public void testJson_WithOtherProps() {

        tester.e28().insertColumns("id", "json")
                .values(35, "[1,2]")
                .values(37, "{}")
                .exec();

        tester.target("/e28/expanded")
                .queryParam("include", "a", E28.JSON.getName(), "z")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"a\":\"A\",\"json\":[1,2],\"z\":\"Z\"}", "{\"a\":\"A\",\"json\":{},\"z\":\"Z\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e4/{id}")
        public DataResponse<E4> getE4_WithIncludeExclude(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).get();
        }

        @GET
        @Path("e19/{id}")
        public DataResponse<E19> getById(@Context UriInfo uriInfo, @PathParam("id") Integer id) {
            return AgJaxrs.select(E19.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).getOne();
        }

        @GET
        @Path("e28")
        public DataResponse<E28> get28(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E28.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e28/expanded")
        public DataResponse<E28> get28Expanded(@Context UriInfo uriInfo) {

            // adding regular properties to see if JSON property can be encoded when other properties are present
            AgEntityOverlay<E28> overlay = AgEntity.overlay(E28.class)
                    .attribute("a", String.class, o -> "A")
                    .attribute("z", String.class, o -> "Z");

            return AgJaxrs.select(E28.class, config)
                    .entityOverlay(overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
