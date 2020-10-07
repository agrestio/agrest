package io.agrest.it;


import io.agrest.cayenne.cayenne.main.E20;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.swagger.api.v1.service.E20Resource;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

public class E20Resource_GeneratedIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(E20Resource.class)
            .entities(E20.class)
            .build();

    @Test
    public void testGET_ById_Exclude() {

        tester.e20().insertColumns("name_col").values("John").exec();

        tester.target("/v1/e20/John")
                .queryParam("exclude", "age", "description").get().wasOk().bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");
    }

    @Test
    public void testPOST_Exclude() {

        tester.target("/v1/e20")
                .queryParam("exclude", "age", "description")
                .post("{\"id\":\"John\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");

        tester.e20().matcher().assertOneMatch();
    }

    @Test
    public void testPUT_ByName() {

        tester.e20().insertColumns("name_col")
                .values("John")
                .values("Brian").exec();

        tester.target("/v1/e20/John")
                .put("{\"age\":28,\"description\":\"zzz\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":\"John\",\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        tester.e20().matcher().eq("age", 28).eq("description", "zzz").assertOneMatch();
    }

    @Test
    public void testDELETE_ByName() {

        tester.e20().insertColumns("name_col")
                .values("John")
                .values("Brian").exec();

        tester.target("/v1/e20/John").delete().wasOk().bodyEquals("{\"success\":true}");

        tester.e20().matcher().assertOneMatch();
        tester.e20().matcher().eq("name_col", "Brian").assertOneMatch();
    }
}
