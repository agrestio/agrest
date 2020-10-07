package io.agrest.it;


import io.agrest.cayenne.cayenne.main.E21;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.swagger.api.v1.service.E21Resource;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

public class E21Resource_GeneratedIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(E21Resource.class)
            .entities(E21.class)
            .build();

    @Test
    public void test_SelectById_MultiId() {

        tester.e21().insertColumns("age", "name")
                .values(18, "John").exec();

        tester.target("/v1/e21")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .queryParam("exclude", "description")
                .get().wasOk().bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");
    }


    @Test
    public void testPOST_Exclude() {

        tester.target("/v1/e21")
                .queryParam("exclude", "description")
                .post("{\"id\":{\"age\":18,\"name\":\"John\"}}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");

        tester.e21().matcher().assertOneMatch();
    }

    @Test
    public void testPUT() {
        tester.e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        tester.target("/v1/e21").queryParam("age", 18)
                .queryParam("name", "John")
                .put("{\"age\":28,\"description\":\"zzz\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":{\"age\":28,\"name\":\"John\"},\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        tester.e21().matcher().eq("age", 28).eq("description", "zzz").assertOneMatch();
    }

    @Test
    public void testDELETE_MultiId() {

        tester.e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        tester.target("/v1/e21")
                .queryParam("age", 18)
                .queryParam("name", "John")

                .delete().wasOk().bodyEquals("{\"success\":true}");

        tester.e21().matcher().assertOneMatch();
        tester.e21().matcher().eq("name", "Brian").eq("age", 27).assertOneMatch();
    }
}
