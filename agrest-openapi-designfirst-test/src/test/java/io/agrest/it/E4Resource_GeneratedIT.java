package io.agrest.it;


import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.swagger.api.v1.service.E4Resource;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

public class E4Resource_GeneratedIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(E4Resource.class)
            .entities(E4.class)
            .build();

    @Test
    public void testGET() {

        tester.e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5).exec();

        tester.target("/v1/e4").get().wasOk().bodyEquals(1,
                "{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":5,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"xxx\"}");
    }

    @Test
    public void testGET_Include() {

        tester.e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5).exec();

        tester.target("/v1/e4").queryParam("include", "id", "cInt").get().wasOk().bodyEquals(1, "{\"id\":1,\"cInt\":5}");
    }

    @Test
    public void testGET_Sort() {

        tester.e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        tester.target("/v1/e4")
                .queryParam("sort", "[{\"property\":\"id\",\"direction\":\"DESC\"}]")
                .queryParam("include", "id")
                .get()
                .wasOk()
                .bodyEquals(3, "{\"id\":3}", "{\"id\":2}", "{\"id\":1}");
    }

    @Test
    public void testGET_Sort_DirIgnored() {

        tester.e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        tester.target("/v1/e4")
                .queryParam("sort", "id")
                .queryParam("dir", "DESC")
                .queryParam("include", "id")
                .get()
                .wasOk()
                // "dir" must be ignored as it is not a part of the method signature
                .bodyEquals(3, "{\"id\":1}", "{\"id\":2}", "{\"id\":3}");
    }

    @Test
    public void testGET_ById() {

        tester.e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        tester.target("/v1/e4/2")
                .queryParam("include", "id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testGET_MapBy() {

        tester.e4().insertColumns("c_varchar", "c_int")
                .values("xxx", 1)
                .values("yyy", 2)
                .values("zzz", 2).exec();

        tester.target("/v1/e4")
                .queryParam("mapBy", "cInt")
                .queryParam("include", "cVarchar")
                .get().wasOk()
                .bodyEqualsMapBy(3,
                        "\"1\":[{\"cVarchar\":\"xxx\"}]",
                        "\"2\":[{\"cVarchar\":\"yyy\"},{\"cVarchar\":\"zzz\"}]");
    }

    @Test
    public void testPOST() {

        tester.target("/v1/e4")
                .post("{\"cVarchar\":\"zzz\"}")
                .wasCreated()
                .replaceId("XID")
                .bodyEquals(1, "{\"id\":XID,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}");

        tester.e4().matcher().assertOneMatch();
    }

    @Test
    public void testPUT() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/v1/e4/8").put("{\"id\":8,\"cVarchar\":\"zzz\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":8," +
                        "\"cBoolean\":null," +
                        "\"cDate\":null," +
                        "\"cDecimal\":null," +
                        "\"cInt\":null," +
                        "\"cTime\":null," +
                        "\"cTimestamp\":null," +
                        "\"cVarchar\":\"zzz\"}");

        tester.e4().matcher().eq("id", 8).eq("c_varchar", "zzz").assertOneMatch();
    }

    @Test
    public void testDelete() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/v1/e4/8").delete().wasOk().bodyEquals("{\"success\":true}");

        tester.e4().matcher().assertOneMatch();
    }

}
